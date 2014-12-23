package com.ivan.xinput;

/**
 * Represents the delta (change) of the axes between two successive polls.
 * 
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputAxesDelta {
	private final XInputAxes lastAxes;
	private final XInputAxes axes;

	protected XInputAxesDelta(final XInputAxes lastAxes, final XInputAxes axes) {
		this.lastAxes = lastAxes;
		this.axes = axes;
	}

	/**
	 * Returns the difference of the Left Thumb X axis between two consecutive polls. A positive value means the stick moved
	 * to the right, while a negative value represents a movement to the left.
	 * 
	 * @return the delta of the Left Thumb X axis
	 */
	public float getLXDelta() {
		return lastAxes.lx - axes.lx;
	}

	/**
	 * Returns the difference of the Left Thumb Y axis between two consecutive polls. A positive value means the stick moved
	 * up, while a negative value represents a down movement.
	 * 
	 * @return the delta of the Left Thumb Y axis
	 */
	public float getLYDelta() {
		return lastAxes.ly - axes.ly;
	}

	/**
	 * Returns the difference of the Right Thumb X axis between two consecutive polls. A positive value means the stick moved
	 * to the right, while a negative value represents a movement to the left.
	 * 
	 * @return the delta of the Right Thumb X axis
	 */
	public float getRXDelta() {
		return lastAxes.rx - axes.rx;
	}

	/**
	 * Returns the difference of the Right Thumb Y axis between two consecutive polls. A positive value means the stick moved
	 * up, while a negative value represents a down movement.
	 * 
	 * @return the delta of the Right Thumb Y axis
	 */
	public float getRYDelta() {
		return lastAxes.ry - axes.ry;
	}

	/**
	 * Returns the difference of the Left Trigger axis between two consecutive polls. A positive value means the trigger was
	 * pressed, while a negative value indicates that the trigger was released.
	 * 
	 * @return the delta of the Left Trigger axis
	 */
	public float getLTDelta() {
		return lastAxes.lt - axes.lt;
	}

	/**
	 * Returns the difference of the Right Trigger axis between two consecutive polls. A positive value means the trigger was
	 * pressed, while a negative value indicates that the trigger was released.
	 * 
	 * @return the delta of the Right Trigger axis
	 */
	public float getRTDelta() {
		return lastAxes.rt - axes.rt;
	}

	/**
	 * Returns the difference of the specified axis between two consecutive polls. Refer to the other methods of this class
	 * to learn what positive and negative value means for each axis.
	 * 
	 * @param axis the axis the get the delta from
	 * @return the delta for the specified axis
	 */
	public float getDelta(final XInputAxis axis) {
		switch (axis) {
			case leftThumbX:
				return getRXDelta();
			case leftThumbY:
				return getLYDelta();
			case rightThumbX:
				return getRXDelta();
			case rightThumbY:
				return getRYDelta();
			case leftTrigger:
				return getLTDelta();
			case rightTrigger:
				return getRTDelta();
			default:
				return 0f;
		}
	}
}
