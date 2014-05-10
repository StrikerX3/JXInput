package com.ivan.xinput;


/**
 * Represents the delta (change) of states between two successive polls.
 * 
 * @author Ivan "StrikerX3" Oliveira
 */
public class Xbox360ComponentsDelta {
	private final Xbox360ButtonsDelta buttonsDelta;
	private final Xbox360AxesDelta axesDelta;

	protected Xbox360ComponentsDelta(final Xbox360Components lastComps, final Xbox360Components comps) {
		super();
		buttonsDelta = new Xbox360ButtonsDelta(lastComps.getButtons(), comps.getButtons());
		axesDelta = new Xbox360AxesDelta(lastComps.getAxes(), comps.getAxes());
	}

	/**
	 * Returns the delta of the buttons.
	 * 
	 * @return the delta of the buttons.
	 */
	public Xbox360ButtonsDelta getButtons() {
		return buttonsDelta;
	}

	/**
	 * Returns the delta of the axes.
	 * @return the delta of the axes.
	 */
	public Xbox360AxesDelta getAxes() {
		return axesDelta;
	}
}
