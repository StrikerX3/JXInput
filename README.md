JXInput
=======

Access XInput devices with Java code.

Visual Studio 2010 project contains the native code, which is already compiled and included in the Java project. It depends on XInput 1.3, which is used by Direct 9.0c games.

Requirements:

The following packages are required to use JXInput. If you have installed and are playing any Direct 9.0c game, you can assume you have the necessary libraries installed into your system. Install these if you are getting the "Could not find dependent libraries" error.

1.  [Microsoft C++ 2010 SP1 Redistributable Package (x86)](http://www.microsoft.com/en-us/download/details.aspx?id=8328) or [Microsoft C++ 2010 SP1 Redistributable Package (x64)](http://www.microsoft.com/en-us/download/details.aspx?id=13523). If your Windows is 64-bit, it doesn't hurt to install both.
2.  [DirectX End-User Runtime](http://www.microsoft.com/en-us/download/details.aspx?id=35).

Usage:

1.  Install the project into your Maven repository (since it is not available in the Central Maven repository).
2.  Include the Maven dependency into your project:

        <dependency>
            <groupId>com.ivan</groupId>
            <artifactId>xinput-device</artifactId>
            <version>0.2</version>
        </dependency>

3.  There is no need to extract or copy the native libraries from the VS project. The natives are included in the jar file and are extracted and loaded at runtime automatically.
4.  In your Java code, import the `com.ivan.xinput.XInputDevice` class.
5.  To retrieve devices:

        // retrieve all devices
        XInputDevice[] devices = XInputDevice.getAllDevices();
        
        // retrieve the device for player 1
        XInputDevice device = XInputDevice.getDeviceFor(0); // or devices[0]
        
6.  Using the device:

        XInputDevice device = ...;
        
        // first we need to poll data
        // will return false if the device is not connected
        if (device.poll()) {
            // retrieve the components
            XInputComponents components = device.getComponents();
            
            XInputButtons buttons = components.getButtons();
            XInputAxes axes = components.getAxes();
            
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

7.  Using deltas (changes in state):

        XInputDevice device = ...;
        if (device.poll()) {
            // retrieve the delta
            XInputComponentsDelta delta = device.getDelta();
            
            XInputButtonsDelta buttons = delta.getButtons();
            XInputAxesDelta axes = delta.getAxes();
            
            // retrieve button state change
            if (buttons.isPressed(XInputButton.a)) {
                // button A was just pressed
            } else if (buttons.isReleased(XInputButton.a)) {
                // button A was just released
            }
            
            // retrieve axis state change
            // the class provides methods for each axis
            // and a method for providing an XInputAxis
            float accelerationDelta = axes.getRTDelta();
            float brakeDelta = axes.getDelta(XInputAxis.leftTrigger);
        } else {
            // controller is not connected; display a message
        }

8.  Using a listener:

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
            public void buttonChanged(final XInputButton button, final boolean pressed) {
                // the given button was just pressed (if pressed == true) or released (pressed == false)
            }
            
            @Override
            public void axisChanged(final XInputAxis axis, final float value, final float delta) {
                // the given axis has changed to value by delta
            }
        };
        
        // whenever the device is polled, listener events will be fired as long as there are changes
        device.poll();

Released under the [MIT License](http://opensource.org/licenses/MIT).
