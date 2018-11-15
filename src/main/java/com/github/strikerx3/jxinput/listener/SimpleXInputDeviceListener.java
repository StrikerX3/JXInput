package com.github.strikerx3.jxinput.listener;

import com.github.strikerx3.jxinput.enums.XInputButton;

/**
 * Provides empty implementations of all {@link XInputDeviceListener} methods for easier subclassing.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class SimpleXInputDeviceListener implements XInputDeviceListener {
    @Override
    public void connected() {
    }

    @Override
    public void disconnected() {
    }

    @Override
    public void buttonChanged(final XInputButton button, final boolean pressed) {
    }
}
