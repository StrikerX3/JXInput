package com.ivan.xinput;

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

    @Override
    public void axisChanged(final XInputAxis axis, final float value, final float delta) {
    }
}
