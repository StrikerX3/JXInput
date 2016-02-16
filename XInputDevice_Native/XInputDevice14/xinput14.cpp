#define WIN32_LEAN_AND_MEAN     // to avoid including unnecessary stuff

#include "com_ivan_xinput_natives_XInputNatives14.h"

#include <Windows.h>
#include <XInput.h>

#pragma comment(lib, "xinput.lib")

JNIEXPORT void JNICALL Java_com_ivan_xinput_natives_XInputNatives14_setEnabled
  (JNIEnv *env, jclass cls, jboolean enabled)
{
	XInputEnable(enabled ? TRUE : FALSE);
}

JNIEXPORT jint JNICALL Java_com_ivan_xinput_natives_XInputNatives14_getCapabilities
  (JNIEnv *env, jclass cls, jint playerNum, jint flags, jobject byteBuffer)
{
	// the byte buffer must be allocatedDirect(20)'d in Java...
	void *bbuf = env->GetDirectBufferAddress(byteBuffer);

	// ... because we're going to write straight into it
	XINPUT_CAPABILITIES *caps = (XINPUT_CAPABILITIES *)bbuf;
	ZeroMemory(caps, sizeof(XINPUT_CAPABILITIES));

	return XInputGetCapabilities(playerNum, flags, caps);
}

JNIEXPORT jint JNICALL Java_com_ivan_xinput_natives_XInputNatives14_getBatteryInformation
  (JNIEnv *env, jclass cls, jint playerNum, jint deviceType, jobject byteBuffer)
{
	// the byte buffer must be allocatedDirect(2)'d in Java...
	void *bbuf = env->GetDirectBufferAddress(byteBuffer);

	// ... because we're going to write straight into it
	XINPUT_BATTERY_INFORMATION *info = (XINPUT_BATTERY_INFORMATION *)bbuf;
	ZeroMemory(info, sizeof(XINPUT_BATTERY_INFORMATION));

	return XInputGetBatteryInformation(playerNum, deviceType, info);
}

JNIEXPORT jint JNICALL Java_com_ivan_xinput_natives_XInputNatives14_getKeystroke
	(JNIEnv *env, jclass cls, jint playerNum, jobject byteBuffer)
{
	// the byte buffer must be allocatedDirect(8)'d in Java...
	void *bbuf = env->GetDirectBufferAddress(byteBuffer);

	// ... because we're going to write straight into it
	XINPUT_KEYSTROKE *info = (XINPUT_KEYSTROKE *)bbuf;
	ZeroMemory(info, sizeof(XINPUT_KEYSTROKE));

	return XInputGetKeystroke(playerNum, 0, info);
}
