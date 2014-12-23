package com.ivan.xinput;


/**
 * Represents the delta (change) of the buttons between two successive polls.
 * 
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputButtonsDelta {
	private final XInputButtons lastButtons;
	private final XInputButtons buttons;

	protected XInputButtonsDelta(final XInputButtons lastButtons, final XInputButtons buttons) {
		this.lastButtons = lastButtons;
		this.buttons = buttons;
	}

	/**
	 * Returns <code>true</code> if the button was pressed (i.e. changed from released to pressed between two consecutive polls).
	 * 
	 * @param button the button
	 * @return <code>true</code> if the button was pressed, <code>false</code> otherwise
	 */
	public boolean isPressed(final XInputButton button) {
		return delta(lastButtons, buttons, button);
	}

	/**
	 * Returns <code>true</code> if the button was released (i.e. changed from pressed to released between two consecutive polls).
	 * 
	 * @param button the button
	 * @return <code>true</code> if the button was released, <code>false</code> otherwise
	 */
	public boolean isReleased(final XInputButton button) {
		return delta(buttons, lastButtons, button);
	}

	/**
	 * Determines if the state of a button was changed from one poll to the following poll.
	 * 
	 * @param from the old state
	 * @param to the new state
	 * @param button the button
	 * @return <code>true</code> if there was a change, <code>false</code> otherwise
	 */
	private boolean delta(final XInputButtons from, final XInputButtons to, final XInputButton button) {
		switch (button) {
			case a:
				return !from.a && to.a;
			case b:
				return !from.b && to.b;
			case x:
				return !from.x && to.x;
			case y:
				return !from.y && to.y;
			case back:
				return !from.back && to.back;
			case start:
				return !from.start && to.start;
			case lShoulder:
				return !from.lShoulder && to.lShoulder;
			case rShoulder:
				return !from.rShoulder && to.rShoulder;
			case lThumb:
				return !from.lThumb && to.lThumb;
			case rThumb:
				return !from.rThumb && to.rThumb;
			case up:
				return !from.up && to.up;
			case down:
				return !from.down && to.down;
			case left:
				return !from.left && to.left;
			case right:
				return !from.right && to.right;
		}
		return false;
	}
}
