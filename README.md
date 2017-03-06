JXInput
=======

Java binding for [XInput](https://msdn.microsoft.com/en-us/library/windows/desktop/ee417001(v=vs.85).aspx).

Visual Studio 2015 solution contains the native code, which is already compiled and included in the Java project. It depends on XInput 1.3, which is used by Direct 9.0c games. XInput 1.4 is also supported through an extended API.

Requirements:

The [Visual C++ Redistributable Packages for Visual Studio 2015](https://www.microsoft.com/en-us/download/details.aspx?id=48145) are required to use JXInput. XInput 1.3 support comes out of the box in Windows 7. XInput 1.4 is only supported in Windows 8 or later.

### Preparation
Option 1: Install to your local Maven repository.

* Install the project into your Maven repository (since it is not available in the Central Maven repository).
* Include the Maven dependency into your project by adding the following to your `pom.xml`:

    ```xml
    <dependency>
        <groupId>com.ivan</groupId>
        <artifactId>xinput-device</artifactId>
        <version>0.8</version>
    </dependency>
    ```

Option 2: Use [JitPack](http://jitpack.io/).

* Add the following to your `pom.xml`:

    ```xml
    <repositories>
       <repository>
            <id>jitpack.io</id>           <!-- JitPack allows github repo to be used as a maven repo -->
            <url>https://jitpack.io</url> <!-- For documentation: http://jitpack.io/ -->
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.github.strikerx3</groupId>
            <artifactId>jxinput</artifactId>
            <version>ca55984</version>    <!-- JXInput 0.8 -->
        </dependency>
    </dependencies>
    ```
    
### Usage
Imports

``` java
import com.ivan.xinput.XInputDevice; // Class for XInput 1.3. Legacy for Win7.
import com.ivan.xinput.XInputDevice14; // Class for XInput 1.4. Includes 1.3 API.
```

Check if the desired version is available

```java
// check if XInput 1.3 is available
if (XInputDevice.isAvailable()) {
    System.out.println("XInput 1.3 is available on this platform.");
}

// check if XInput 1.4 is available
if (XInputDevice14.isAvailable()) {
    System.out.println("XInput 1.4 is available on this platform.");
}
```

Check which DLL version was loaded

```java
// get the DLL version
System.out.println("Native library version: " + XInputDevice.getLibraryVersion());
```
    
Retrieve devices

``` java
// retrieve all devices
XInputDevice[] devices = XInputDevice.getAllDevices();

// retrieve the device for player 1
XInputDevice device = XInputDevice.getDeviceFor(0); // or devices[0]

// use XInputDevice14 if you want to use the XInput 1.4 functions
```
    
Using the device ([XInputGetState](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetstate(v=vs.85).aspx))

```java
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
    if (buttons.a) {
        // the A button is currently pressed
    }

    // check if Guide button is supported
    if (XInputDevice.isGuideButtonSupported()) {
        // use it
        if (buttons.guide) {
            // the Guide button is currently pressed
        }
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
```

Using deltas (changes in state between polls)

```java
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
```

Using a listener

``` java
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
};

// whenever the device is polled, listener events will be fired as long as there are changes
device.poll();
```

Vibration ([XInputSetState](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputsetstate(v=vs.85).aspx))

``` java
XInputDevice device = ...;

// vibration speeds from 0 to 65535
//   where 0 = no vibration
//   and 65535 = maximum vibration
// values out of range throw IllegalArgumentException
int leftMotor = ...;
int rightMotor = ...;

device.setVibration(leftMotor, rightMotor);
```
    
## XInput 1.4 specific

Enable or disable the XInput reporting state ([XInputEnable](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputenable(v=vs.85).aspx))

``` java
// use this when your application loses focus
XInputDevice14.setEnabled(false);
// - polling will return neutral data regardless of actual state (e.g. sticks at rest, buttons released)
// - vibration settings will be ignored

// use this when your application regains focus
XInputDevice14.setEnabled(true);
// - polling will return the actual state of the controller
// - vibration settings will be applied
```
	
Retrieve battery information from a device ([XInputGetBatteryInformation](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetbatteryinformation(v=vs.85).aspx))

``` java
XInputDevice14 device = ...;

// retrieves the gamepad battery data
XInputBatteryInformation gamepadBattInfo = device.getBatteryInformation(XInputBatteryDeviceType.GAMEPAD);
System.out.println("Gamepad battery: " + gamepadBattInfo.getType() + ", " + gamepadBattInfo.getLevel());

// check battery level
if (gamepadBattInfo.getLevel() == XInputBatteryLevel.LOW) {
    System.out.println("  Battery is low! Recharge or replace batteries.");
}
```
    
Retrieve device capabilities ([XInputGetCapabilities](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinput_capabilities(v=vs.85).aspx))

``` java
XInputDevice14 device = ...;

XInputCapabilities caps = device.getCapabilities();
System.out.println("Device type: " + caps.getType());
System.out.println("Device subtype: " + caps.getSubType());

// caps.getSupportedButtons() returns a Set<XInputButton> with the supported buttons
// caps.getResolutions() returns an object with the resolutions of all axes
```
    
Retrieve a keystroke ([XInputGetKeystroke](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetkeystroke(v=vs.85).aspx))

``` java
XInputDevice14 device = ...;

XInputKeystroke keystroke = device.getKeystroke();
// use keystroke.isKeyDown(), .isKeyUp() and .isRepeat() to check the kind of keystroke
// use keystroke.getVirtualKey() to get the virtual key code (constants available in XInputVirtualKeyCodes)
// use keystroke.getUnicode() to get the Unicode character
```

### Debugging

JXInput comes with both debug and release versions of the native libraries. By default, the release libraries are used. To load the debug libraries, set the system property `native.debug` to `true`.

Released under the [MIT License](http://opensource.org/licenses/MIT).
