package com.github.strikerx3.jxinput;

import java.nio.ByteBuffer;

import com.github.strikerx3.jxinput.enums.XInputBatteryLevel;
import com.github.strikerx3.jxinput.enums.XInputBatteryType;

/**
 * Contains information about the device's battery.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputBatteryInformation {
    private final XInputBatteryType type;
    private final XInputBatteryLevel level;

    XInputBatteryInformation(final ByteBuffer buffer) {
        // typedef struct _XINPUT_BATTERY_INFORMATION
        // {
        //     BYTE BatteryType;
        //     BYTE BatteryLevel;
        // } XINPUT_BATTERY_INFORMATION, *PXINPUT_BATTERY_INFORMATION;

        type = XInputBatteryType.fromNative(buffer.get());
        level = XInputBatteryLevel.fromNative(buffer.get());
    }

    public XInputBatteryType getType() {
        return type;
    }

    public XInputBatteryLevel getLevel() {
        return level;
    }
}
