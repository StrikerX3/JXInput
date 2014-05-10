JXInputDevice
=============

Access XInput devices with Java code.

Visual Studio 2010 project contains the native code, which is already compiled and included in the Java project.

Usage:

1.  Include the Maven dependency into your project:

        <dependency>
            <groupId>com.ivan</groupId>
            <artifactId>xinput-device</artifactId>
            <version>0.1</version>
        </dependency>

2.  There is no need to extract or copy the native libraries from the VS project. The natives are included in the jar file and are extracted and loaded at runtime automatically.
3.  In your Java code, import the `com.ivan.xinput.XInputDevice` class.
4.  To retrieve devices:

        // retrieve all devices
        XInputDevice[] devices = XInputDevice.getAllDevices();
        
        // retrieve the device for player 1
        XInputDevice device = XInputDevice.getDeviceFor(0); // or devices[0]
        
5.  Using the device:

        XInputDevice device = ...;
        
        // first we need to poll data
        // will return false if the device is not connected
        if (device.poll()) {
            // retrieve the components
            Xbox360Components components = device.getComponents();
            
            Xbox360Buttons buttons = components.getButtons();
            Xbox360Axes axes = components.getAxes();
            
            // buttons and axes have public fields
            
            // retrieve button state
            if (button.a) {
                // button A is currently pressed
            }
            
            // retrieve axis state
            float acceleration = axes.rt;
            float brake = axes.lt;
        } else {
            // controller is not connected; display a message
        }
        
        // this is exactly the same as above
        device.poll();
        if (device.isConnected()) {
            // ...
        } else {
            // ...
        }

6.  Using deltas (changes in state):

        XInputDevice device = ...;
        if (device.poll()) {
            // retrieve the delta
            Xbox360ComponentsDelta delta = device.getDelta();
            
            Xbox360ButtonsDelta buttons = delta.getButtons();
            Xbox360AxesDelta axes = delta.getAxes();
            
            // retrieve button state change
            if (buttons.isPressed(Xbox360Button.a)) {
                // button A was just pressed
            } else if (buttons.isReleased(Xbox360Button.a)) {
                // button A was just released
            }
            
            // retrieve axis state change
            // the class provides methods for each axis
            // and a method for providing an Xbox360Axis
            float accelerationDelta = axes.getRTDelta();
            float brakeDelta = axes.getDelta(Xbox360Axis.leftTrigger);
        } else {
            // controller is not connected; display a message
        }

7.  Using a listener:

        XInputDevice device = ...;
        
        // the SimpleXInputDeviceListener allows us to implement only the methods
        // we actually need
        XInputDeviceListener listener = new SimpleXInputDeviceListener() {
            @Override
            public void connected() {
                // resume the game
            }
            
            @Override
            public void disconnected() {
                // pause the game and display a message
            }
            
            @Override
            public void buttonChanged(final Xbox360Button button, final boolean pressed) {
                // the given button was just pressed (if pressed == true) or released (pressed == false)
            }
            
            @Override
            public void axisChanged(final Xbox360Axis axis, final float value, final float delta) {
                // the given axis has changed to value by delta
            }
        };

Released under the [MIT License](http://opensource.org/licenses/MIT).
