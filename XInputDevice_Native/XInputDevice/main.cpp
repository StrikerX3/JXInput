#define WIN32_LEAN_AND_MEAN     // to avoid including unnecessary stuff

#include "com_ivan_xinput_XInputDevice.h"

#include <Windows.h>
#include <XInput.h>

JNIEXPORT jint JNICALL Java_com_ivan_xinput_XInputDevice_pollDevice
  (JNIEnv *env, jclass cls, jint playerNum, jobject byteBuffer)
{
	// the byte buffer must be allocatedDirect(16)'d in Java...
	void *bbuf = env->GetDirectBufferAddress(byteBuffer);  

	// ... because we're going to write straight into it
	XINPUT_STATE *state = (XINPUT_STATE *)bbuf;
	ZeroMemory(state, sizeof(XINPUT_STATE));

	return XInputGetState(playerNum, state);
}

JNIEXPORT jint JNICALL Java_com_ivan_xinput_XInputDevice_setVibration
  (JNIEnv *env, jclass cls, jint playerNum, jshort leftMotor, jshort rightMotor)
{
	XINPUT_VIBRATION vib;
	vib.wLeftMotorSpeed = leftMotor;
	vib.wRightMotorSpeed = rightMotor;

	return XInputSetState(playerNum, &vib);
}