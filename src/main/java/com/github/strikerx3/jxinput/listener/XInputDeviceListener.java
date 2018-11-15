package com.github.strikerx3.jxinput.listener;

import com.github.strikerx3.jxinput.enums.XInputButton;

/**
 * Listens to all XInput events.
 * The {@link SimpleXInputDeviceListener} class provides empty implementations of the methods in this interface
 * for easier subclassing.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public interface XInputDeviceListener {
    /**
     * Called when the device is connected.
     */
    void connected();

    /**
     * Called when the device is disconnected.
     */
    void disconnected();

    /**
     * Called when a button is pressed or released.
     *
     * @param button the button
     * @param pressed <code>true</code> if the button was pressed, <code>false</code> if released.
     */
    void buttonChanged(final XInputButton button, final boolean pressed);
}
