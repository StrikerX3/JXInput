package com.ivan.xinput.listener;

import com.ivan.xinput.enums.XInputAxis;
import com.ivan.xinput.enums.XInputButton;


/**
 * Listens to all XInput events.
 * 
 * @author Ivan "StrikerX3" Oliveira
 */
public interface XInputDeviceListener {
	/**
	 * Called when the device is disconnected.
	 */
	void connected();

	/**
	 * Called when the device is connected.
	 */
	void disconnected();

	/**
	 * Called when a button is pressed or released.
	 * 
	 * @param button the button
	 * @param pressed <code>true</code> if the button was pressed, <code>false</code> if released.
	 */
	void buttonChanged(final XInputButton button, final boolean pressed);

	/**
	 * Called when an axis is changed.
	 * 
	 * @param axis the axis
	 * @param value the new value
	 * @param delta the value delta (difference between current and last values)
	 */
	void axisChanged(final XInputAxis axis, final float value, float delta);
}
