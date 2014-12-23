package com.ivan.xinput;

/**
 * Contains the states of all XInput axes.
 * 
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputAxes {
	public float lx, ly;
	public float rx, ry;

	public float lt, rt;

	public int dpad;

	public static final int DPAD_CENTER = -1;
	public static final int DPAD_UP_LEFT = 0;
	public static final int DPAD_UP = 1;
	public static final int DPAD_UP_RIGHT = 2;
	public static final int DPAD_RIGHT = 3;
	public static final int DPAD_DOWN_RIGHT = 4;
	public static final int DPAD_DOWN = 5;
	public static final int DPAD_DOWN_LEFT = 6;
	public static final int DPAD_LEFT = 7;

	protected XInputAxes() {
		reset();
	}

	/**
	 * Gets the value from the specified axis.
	 * 
	 * @param axis the axis
	 * @return the value of the axis
	 */
	public float get(final XInputAxis axis) {
		switch (axis) {
			case leftThumbX:
				return lx;
			case leftThumbY:
				return ly;
			case rightThumbX:
				return rx;
			case rightThumbY:
				return ry;
			case leftTrigger:
				return lt;
			case rightTrigger:
				return rt;
			case dpad:
				return dpad;
			default:
				return 0f;
		}
	}

	/**
	 * Resets the state of all axes.
	 */
	protected void reset() {
		lx = ly = 0f;
		rx = ry = 0f;

		lt = rt = 0f;

		dpad = DPAD_CENTER;
	}

	/**
	 * Copies the state of all axes from the specified state.
	 * 
	 * @param buttons the state to copy from
	 */
	protected void copy(final XInputAxes axes) {
		lx = axes.lx;
		ly = axes.ly;

		rx = axes.rx;
		ry = axes.ry;

		lt = axes.lt;
		rt = axes.rt;

		dpad = axes.dpad;
	}

	/**
	 * Returns an integer representing the current direction of the D-Pad.
	 * 
	 * @param up the up button state
	 * @param down the down button state
	 * @param left the left button state
	 * @param right the right button state
	 * @return one of the <code>DPAD_*</code> values of this class
	 */
	public static int dpadFromButtons(final boolean up, final boolean down, final boolean left, final boolean right) {
		boolean u = up;
		boolean d = down;
		boolean l = left;
		boolean r = right;

		// Fix invalid buttons (cancel up-down and left-right)
		if (u && d) {
			u = d = false;
		}
		if (l && r) {
			l = r = false;
		}

		// Now we have 9 cases:
		//         left             center        right
		// up      DPAD_UP_LEFT     DPAD_UP       DPAD_UP_RIGHT
		// center  DPAD_LEFT        DPAD_CENTER   DPAD_RIGHT
		// down    DPAD_DOWN_LEFT   DPAD_DOWN     DPAD_DOWN_RIGHT

		if (up) {
			if (left) {
				return DPAD_UP_LEFT;
			}
			if (right) {
				return DPAD_UP_RIGHT;
			}
			return DPAD_UP;
		}
		if (down) {
			if (left) {
				return DPAD_DOWN_LEFT;
			}
			if (right) {
				return DPAD_DOWN_RIGHT;
			}
			return DPAD_DOWN;
		}
		// vertical center
		if (left) {
			return DPAD_LEFT;
		}
		if (right) {
			return DPAD_RIGHT;
		}
		return DPAD_CENTER;
	}
}
