package com.ivan.xinput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents all XInput devices registered in the system.
 * Use the {@link #getAllDevices()} or {@link #getDeviceFor(int)} methods to start using the devices.
 *
 * @author Ivan "StrikerX3" Oliveira
 * @see Xbox360Components
 * @see Xbox360ComponentsDelta
 */
public class XInputDevice {
    private final int playerNum;
    private final ByteBuffer buffer; // Contains the XINPUT_STATE struct
    private final Xbox360Components lastComponents;
    private final Xbox360Components components;
    private final Xbox360ComponentsDelta delta;

    public static final int MAX_PLAYERS = 4;

    private boolean lastConnected;
    private boolean connected;

    private final List<XInputDeviceListener> listeners;

    private static final XInputDevice[] DEVICES = new XInputDevice[MAX_PLAYERS];
    static {
        final String arch = System.getProperty("os.arch").contains("64") ? "64" : "32";
        try {
            final File dir = new File("lib/native");
            dir.mkdirs();

            final String libName = "XInputReader-" + arch + ".dll";
            final File file = new File(dir, libName);
            try {
                final InputStream input = XInputDevice.class.getClassLoader().getResourceAsStream("lib/native/" + libName);
                try {
                    file.createNewFile();

                    final FileOutputStream fos = new FileOutputStream(file);
                    try {
                        final byte[] buf = new byte[65536];
                        int len;
                        while ((len = input.read(buf)) > -1) {
                            fos.write(buf, 0, len);
                        }
                    } finally {
                        fos.close();
                    }
                } finally {
                    input.close();
                }
            } catch (final Exception e) {
                throw new Error("Could not load native libraries", e);
            }

            System.load(file.getAbsolutePath());
        } catch (final UnsatisfiedLinkError e) {
            throw new Error("XInputDevice could not find required library: XInputReader-" + arch, e);
        }

        for (int i = 0; i < MAX_PLAYERS; i++) {
            DEVICES[i] = new XInputDevice(i);
        }
    }

    public static void main(final String[] args) {
        System.out.println(XInputDevice.getDeviceFor(1).isConnected());
    }

    private XInputDevice(final int playerNum) {
        this.playerNum = playerNum;
        buffer = ByteBuffer.allocateDirect(16); // sizeof(XINPUT_STATE)
        buffer.order(ByteOrder.nativeOrder());

        lastComponents = new Xbox360Components();
        components = new Xbox360Components();
        delta = new Xbox360ComponentsDelta(lastComponents, components);

        listeners = new LinkedList<XInputDeviceListener>();

        poll();
    }

    /**
     * Returns an array containing all registered XInput devices.
     *
     * @return all XInput devices
     */
    public static XInputDevice[] getAllDevices() {
        return DEVICES.clone();
    }

    /**
     * Returns the XInput device for the specified player.
     *
     * @param playerNum the player number
     * @return the XInput device for the specified player
     */
    public static XInputDevice getDeviceFor(final int playerNum) {
        if (playerNum < 0 || playerNum >= MAX_PLAYERS) {
            throw new IllegalArgumentException("Invalid player number: " + playerNum + ". Must be between 0 and " + (MAX_PLAYERS - 1));
        }
        return DEVICES[playerNum];
    }

    /**
     * Adds an event listener that will react to changes in the input.
     *
     * @param listener the listener
     */
    public void addListener(final XInputDeviceListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a registered event listener
     *
     * @param listener the listener
     */
    public void removeListener(final XInputDeviceListener listener) {
        listeners.remove(listener);
    }

    /**
     * Reads input from the device and updates components.
     *
     * @return <code>false</code> if the device was not connected
     */
    public boolean poll() {
        final int ret = pollDevice(playerNum, buffer);
        if (ret == ERROR_DEVICE_NOT_CONNECTED) {
            setConnected(false);
            return false;
        }
        if (ret != ERROR_SUCCESS) {
            setConnected(false);
            throw new Error("Could not read controller state: " + ret);
        }
        setConnected(true);

        // typedef struct _XINPUT_STATE
        // {
        //     DWORD                               dwPacketNumber;
        //     XINPUT_GAMEPAD                      Gamepad;
        // } XINPUT_STATE, *PXINPUT_STATE;

        // typedef struct _XINPUT_GAMEPAD
        // {
        //     WORD                                wButtons;
        //     BYTE                                bLeftTrigger;
        //     BYTE                                bRightTrigger;
        //     SHORT                               sThumbLX;
        //     SHORT                               sThumbLY;
        //     SHORT                               sThumbRX;
        //     SHORT                               sThumbRY;
        // } XINPUT_GAMEPAD, *PXINPUT_GAMEPAD;

        /*int packetNumber = */buffer.getInt(); // can be safely ignored
        final short btns = buffer.getShort();
        final byte leftTrigger = buffer.get();
        final byte rightTrigger = buffer.get();
        final short thumbLX = buffer.getShort();
        final short thumbLY = buffer.getShort();
        final short thumbRX = buffer.getShort();
        final short thumbRY = buffer.getShort();
        buffer.flip();

        lastComponents.copy(components);

        final boolean up = (btns & XINPUT_GAMEPAD_DPAD_UP) != 0;
        final boolean down = (btns & XINPUT_GAMEPAD_DPAD_DOWN) != 0;
        final boolean left = (btns & XINPUT_GAMEPAD_DPAD_LEFT) != 0;
        final boolean right = (btns & XINPUT_GAMEPAD_DPAD_RIGHT) != 0;

        final Xbox360Axes axes = components.getAxes();
        axes.lx = thumbLX / 32768f;
        axes.ly = thumbLY / 32768f;
        axes.rx = thumbRX / 32768f;
        axes.ry = thumbRY / 32768f;
        axes.lt = (leftTrigger & 0xff) / 255f;
        axes.rt = (rightTrigger & 0xff) / 255f;
        axes.dpad = Xbox360Axes.dpadFromButtons(up, down, left, right);

        final Xbox360Buttons buttons = components.getButtons();
        buttons.a = (btns & XINPUT_GAMEPAD_A) != 0;
        buttons.b = (btns & XINPUT_GAMEPAD_B) != 0;
        buttons.x = (btns & XINPUT_GAMEPAD_X) != 0;
        buttons.y = (btns & XINPUT_GAMEPAD_Y) != 0;
        buttons.back = (btns & XINPUT_GAMEPAD_BACK) != 0;
        buttons.start = (btns & XINPUT_GAMEPAD_START) != 0;
        buttons.lShoulder = (btns & XINPUT_GAMEPAD_LEFT_SHOULDER) != 0;
        buttons.rShoulder = (btns & XINPUT_GAMEPAD_RIGHT_SHOULDER) != 0;
        buttons.lThumb = (btns & XINPUT_GAMEPAD_LEFT_THUMB) != 0;
        buttons.rThumb = (btns & XINPUT_GAMEPAD_RIGHT_THUMB) != 0;
        buttons.up = up;
        buttons.down = down;
        buttons.left = left;
        buttons.right = right;

        processDelta();
        return true;
    }

    private void setConnected(final boolean state) {
        lastConnected = connected;
        connected = state;
        for (final XInputDeviceListener listener : listeners) {
            if (connected && !lastConnected) {
                listener.connected();
            } else if (!connected && lastConnected) {
                listener.disconnected();
            }
        }
    }

    private void processDelta() {
        final Xbox360ButtonsDelta buttons = delta.getButtons();
        final Xbox360AxesDelta axes = delta.getAxes();
        for (final XInputDeviceListener listener : listeners) {
            for (final Xbox360Button button : Xbox360Button.values()) {
                if (buttons.isPressed(button)) {
                    listener.buttonChanged(button, true);
                } else if (buttons.isReleased(button)) {
                    listener.buttonChanged(button, false);
                }
            }
            for (final Xbox360Axis axis : Xbox360Axis.values()) {
                final float delta = axes.getDelta(axis);
                if (delta != 0f) {
                    listener.axisChanged(axis, components.getAxes().get(axis), delta);
                }
            }
        }
    }

    /**
     * Sets the vibration of the controller. Returns <code>false</code> if the device was not connected.
     *
     * @param leftMotor the left motor speed
     * @param rightMotor the right motor speed
     * @return <code>false</code> if the device was not connected
     */
    public boolean setVibration(final short leftMotor, final short rightMotor) {
        return setVibration(playerNum, leftMotor, rightMotor) != ERROR_DEVICE_NOT_CONNECTED;
    }

    /**
     * Returns the state of the Xbox 360 controller components before the last poll.
     *
     * @return the state of the Xbox 360 controller components before the last poll.
     */
    public Xbox360Components getLastComponents() {
        return lastComponents;
    }

    /**
     * Returns the state of the Xbox 360 controller components at the last poll.
     *
     * @return the state of the Xbox 360 controller components at the last poll.
     */
    public Xbox360Components getComponents() {
        return components;
    }

    /**
     * Returns the difference between the last two states of the Xbox 360 controller components.
     *
     * @return the difference between the last two states of the Xbox 360 controller components.
     */
    public Xbox360ComponentsDelta getDelta() {
        return delta;
    }

    /**
     * Returns a boolean indicating whether this device is connected.
     *
     * @return <code>true</code> if the device is connected, <code>false</code> otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Returns the player number that this device represents.
     *
     * @return the player number that this device represents.
     */
    public int getPlayerNum() {
        return playerNum;
    }

    private static native int pollDevice(int playerNum, ByteBuffer data);

    private static native int setVibration(int playerNum, short leftMotor, short rightMotor);

    // Xbox 360 controller button masks
    private static final short XINPUT_GAMEPAD_DPAD_UP = 0x0001;
    private static final short XINPUT_GAMEPAD_DPAD_DOWN = 0x0002;
    private static final short XINPUT_GAMEPAD_DPAD_LEFT = 0x0004;
    private static final short XINPUT_GAMEPAD_DPAD_RIGHT = 0x0008;
    private static final short XINPUT_GAMEPAD_START = 0x0010;
    private static final short XINPUT_GAMEPAD_BACK = 0x0020;
    private static final short XINPUT_GAMEPAD_LEFT_THUMB = 0x0040;
    private static final short XINPUT_GAMEPAD_RIGHT_THUMB = 0x0080;
    private static final short XINPUT_GAMEPAD_LEFT_SHOULDER = 0x0100;
    private static final short XINPUT_GAMEPAD_RIGHT_SHOULDER = 0x0200;
    private static final short XINPUT_GAMEPAD_A = 0x1000;
    private static final short XINPUT_GAMEPAD_B = 0x2000;
    private static final short XINPUT_GAMEPAD_X = 0x4000;
    private static final short XINPUT_GAMEPAD_Y = (short) 0x8000;
    // Windows error codes
    private static final int ERROR_SUCCESS = 0;
    private static final int ERROR_DEVICE_NOT_CONNECTED = 1167;
}
