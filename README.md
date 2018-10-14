JXInput
=======

Java binding for [XInput](https://msdn.microsoft.com/en-us/library/windows/desktop/ee417001(v=vs.85).aspx).

Visual Studio 2015 solution contains the native code, which is already compiled and included in the Java project. It depends on XInput 1.3, which is used by Direct 9.0c games. XInput 1.4 is also supported through an extended API.

# Requirements

The [Visual C++ Redistributable Packages for Visual Studio 2015](https://www.microsoft.com/en-us/download/details.aspx?id=48145) are required to use JXInput. XInput 1.3 support comes out of the box in Windows 7, Vista and XP SP1. XInput 1.4 is only supported in Windows 8 or later.

# Usage

If you just want to use the library on your project, simply head to the [releases page](https://github.com/StrikerX3/JXInput/releases/), grab the latest version and include it in your project. If you prefer to use a Maven-compatible build system, use one of the options below:

## Install to your local Maven repository

1. Clone this project
2. Install to your local Maven repository (since it is not available in the Central Maven repository):

    ```
    cd JXInput\XInputDevice_Java
    mvn clean install
    ```

3. Include the Maven dependency into your project by adding the following to your `pom.xml`:

    ```xml
    <dependency>
        <groupId>com.ivan</groupId>
        <artifactId>xinput-device</artifactId>
        <version>0.9</version>
    </dependency>
    ```

## Use [JitPack](http://jitpack.io/)

Add the following to your `pom.xml`:

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
        <version>0.9</version>    <!-- Use any released version, commit hash or branch-SNAPSHOT here -->
    </dependency>
</dependencies>
```
    
# Code examples

The entry points of the library are the classes `com.ivan.xinput.XInputDevice` and `com.ivan.xinput.XInputDevice14`.

`XInputDevice`contains all XInput 1.3 functionality and is compatible with all versions of Windows out of the box since XP SP1. `XInputDevice14` extends that class with features specific to XInput 1.4 and requires Windows 8 and later. Not all features are supported; most notably missing are the [audio](https://docs.microsoft.com/en-us/windows/desktop/api/xinput/nf-xinput-xinputgetaudiodeviceids) [functions](https://docs.microsoft.com/en-us/windows/desktop/api/XInput/nf-xinput-xinputgetdsoundaudiodeviceguids).

In order to check if the desired XInput version is available at runtime, you can use the static method `isAvailable()` on those classes:

```java
// Check if XInput 1.3 is available
if (XInputDevice.isAvailable()) {
    // XInput 1.3 is available on this platform
}

// Check if XInput 1.4 is available
if (XInputDevice14.isAvailable()) {
    // XInput 1.4 is available on this platform
}
```

You may also be interested in checking which DLL version was loaded:

```java
// Get the DLL version, which can be one of the following:
// - XInputLibraryVersion.NONE: no XInput available
// - XInputLibraryVersion.XINPUT_1_4: XInput 1.4 (Windows 8 and later)
// - XInputLibraryVersion.XINPUT_1_3: XInput 1.3 (Windows XP SP1 and later)
// - XInputLibraryVersion.XINPUT_9_1_0: XInput9 1.0 (Windows Vista only)
XInputLibraryVersion libVersion = XInputDevice.getLibraryVersion();
```

## Using `XInputDevice`: XInput 1.3

Supported on Windows XP SP1, Vista, 7, 8, 8.1 and 10.
    
### Retrieve devices

``` java
// Retrieve all devices
XInputDevice[] devices = XInputDevice.getAllDevices();

// Retrieve the device for player 1
XInputDevice device = XInputDevice.getDeviceFor(0); // or devices[0]
```
    
### Using the device ([XInputGetState](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetstate(v=vs.85).aspx))

```java
XInputDevice device = ...;

// First we need to poll data.
// poll() will return false if the device is not connected
if (device.poll()) {
    // Retrieve the components
    XInputComponents components = device.getComponents();

    XInputButtons buttons = components.getButtons();
    XInputAxes axes = components.getAxes();

    // Buttons and axes have public fields (although this is not idiomatic Java)

    // Retrieve button state
    if (buttons.a) {
        // The A button is currently pressed
    }

    // Check if Guide button is supported
    if (XInputDevice.isGuideButtonSupported()) {
        // Use it
        if (buttons.guide) {
            // The Guide button is currently pressed
        }
    }

    // Retrieve axis state
    float acceleration = axes.rt;
    float brake = axes.lt;
} else {
    // Controller is not connected; display a message
}

// This is exactly the same as above
device.poll();
if (device.isConnected()) {
    // ...
} else {
    // ...
}
```

### Using deltas (changes in state between polls)

```java
XInputDevice device = ...;

if (device.poll()) {
    // Retrieve the delta
    XInputComponentsDelta delta = device.getDelta();

    XInputButtonsDelta buttons = delta.getButtons();
    XInputAxesDelta axes = delta.getAxes();

    // Retrieve button state change
    if (buttons.isPressed(XInputButton.a)) {
        // Button A was just pressed
    } else if (buttons.isReleased(XInputButton.a)) {
        // Button A was just released
    }

    // Retrieve axis state change.
    // The class provides methods for each axis and a method for providing an XInputAxis
    float accelerationDelta = axes.getRTDelta();
    float brakeDelta = axes.getDelta(XInputAxis.leftTrigger);
} else {
    // Controller is not connected; display a message
}
```

### Using a listener

```java
XInputDevice device = ...;

// The SimpleXInputDeviceListener allows us to implement only the methods we actually need
XInputDeviceListener listener = new SimpleXInputDeviceListener() {
    @Override
    public void connected() {
        // Resume the game
    }

    @Override
    public void disconnected() {
        // Pause the game and display a message
    }

    @Override
    public void buttonChanged(final XInputButton button, final boolean pressed) {
        // The given button was just pressed (if pressed == true) or released (pressed == false)
    }
};

// Whenever the device is polled, listener events will be fired as long as there are changes
device.poll();
```

### Vibration ([XInputSetState](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputsetstate(v=vs.85).aspx))

```java
XInputDevice device = ...;

// Vibration speeds from 0 to 65535
//   where 0 = no vibration
//   and 65535 = maximum vibration
// Values out of range throw IllegalArgumentException
int leftMotor = ...;
int rightMotor = ...;

device.setVibration(leftMotor, rightMotor);
```
    
## Using `XInputDevice14`: XInput 1.4

Supported on Windows 8, 8.1 and 10.

All methods in `XInputDevice` are also available in `XInputDevice14`. For instance, you can poll a device by using:

``` java
// Retrieve the device for player 1
XInputDevice14 device = XInputDevice14.getDeviceFor(0); // or devices[0]

// Poll the device
if (device.poll()) {
    ...
}
```

### Enable or disable the XInput reporting state ([XInputEnable](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputenable(v=vs.85).aspx))

``` java
// Use this when your application loses focus
XInputDevice14.setEnabled(false);
// - Polling will return neutral data regardless of actual state (e.g. sticks at rest, buttons released)
// - Vibration settings will be ignored

// Use this when your application regains focus
XInputDevice14.setEnabled(true);
// - Polling will return the actual state of the controller
// - Vibration settings will be applied
```
	
### Retrieve battery information from a device ([XInputGetBatteryInformation](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetbatteryinformation(v=vs.85).aspx))

```java
XInputDevice14 device = ...;

// Retrieves the gamepad battery data
XInputBatteryInformation gamepadBattInfo = device.getBatteryInformation(XInputBatteryDeviceType.GAMEPAD);
// gamepadBattInfo.getLevel() contains the battery charge level, one of the values of XInputBatteryLevel: EMPTY, LOW, MEDIUM or FULL.
// gamepadBattInfo.getType() contains the battery type:
// - XInputBatteryType.DISCONNECTED: the controller is disconnected
// - XInputBatteryType.WIRED: wired controller
// - XInputBatteryType.ALKALINE: using alkaline batteries
// - XInputBatteryType.NIMH: using rechargeable nickel-metal hydride batteries
// - XInputBatteryType.UNKNOWN: using an unknown type of battery

// Check battery level
if (gamepadBattInfo.getLevel() == XInputBatteryLevel.LOW) {
    // Battery is low! Might be useful to warn the user to recharge or replace batteries
}
```
    
### Retrieve device capabilities ([XInputGetCapabilities](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinput_capabilities(v=vs.85).aspx))

```java
XInputDevice14 device = ...;

XInputCapabilities caps = device.getCapabilities();
// caps.getType() returns the type of the device, which is always XInputDeviceType.GAMEPAD
// caps.getSubType() returns the subtype, one of the [XInputDeviceSubType](https://github.com/StrikerX3/JXInput/blob/master/XInputDevice_Java/src/main/java/com/ivan/xinput/enums/XInputDeviceSubType.java) enum values
// caps.getSupportedButtons() returns a Set<XInputButton> with the supported buttons
// caps.getResolutions() returns an object with the resolutions of all axes as a bit mask
```
    
### Retrieve a keystroke ([XInputGetKeystroke](https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinputgetkeystroke(v=vs.85).aspx))

```java
XInputDevice14 device = ...;

XInputKeystroke keystroke = device.getKeystroke();
// Use keystroke.isKeyDown(), .isKeyUp() and .isRepeat() to check the kind of keystroke
// Use keystroke.getVirtualKey() to get the virtual key code (constants available in XInputVirtualKeyCodes)
// Use keystroke.getUnicode() to get the Unicode character
```

# Debugging

JXInput comes with both debug and release versions of the native libraries. By default, the release libraries are used. To load the debug libraries, set the system property `native.debug` to `true` as a JVM argument: `-Dnative.debug=true`.

Released under the [MIT License](http://opensource.org/licenses/MIT).
