package com.ivan.xinput;

/**
 * Contains all components for an Xbox 360 controller.
 * 
 * @author Ivan "StrikerX3" Oliveira
 */
public class Xbox360Components {
	private final Xbox360Buttons buttons;
	private final Xbox360Axes axes;

	protected Xbox360Components() {
		buttons = new Xbox360Buttons();
		axes = new Xbox360Axes();
	}

	/**
	 * Returns the Xbox 360 button states.
	 * 
	 * @return the Xbox 360 button states
	 */
	public Xbox360Buttons getButtons() {
		return buttons;
	}

	/**
	 * Returns the Xbox 360 axis states.
	 * @return the Xbox 360 axis states
	 */
	public Xbox360Axes getAxes() {
		return axes;
	}

	/**
	 * Resets the components to their default values.
	 */
	protected void reset() {
		buttons.reset();
		axes.reset();
	}

	/**
	 * Copies the values from the specified components.
	 * 
	 * @param components the components to copy the values from
	 */
	protected void copy(final Xbox360Components components) {
		buttons.copy(components.getButtons());
		axes.copy(components.getAxes());
	}
}
