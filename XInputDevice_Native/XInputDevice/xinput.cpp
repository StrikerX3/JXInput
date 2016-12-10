#define WIN32_LEAN_AND_MEAN     // to avoid including unnecessary stuff

#include "com_ivan_xinput_natives_XInputNatives.h"

#include <Windows.h>
#include <XInput.h>
#include <ShlObj.h>

// taken from https://github.com/speps/XInputDotNet/blob/master/XInputInterface/GamePad.cpp
namespace
{
	enum libver { LIBVER_none, LIBVER_xinput1_4, LIBVER_xinput1_3, LIBVER_xinput9_1_0 };
	typedef DWORD(__stdcall *XInputGetStatePointer)(DWORD dwUserIndex, XINPUT_STATE* pState);
	typedef DWORD(__stdcall *XInputSetStatePointer)(DWORD dwUserIndex, XINPUT_VIBRATION* pVibration);

	class XInputLoader
	{
	public:
		XInputLoader()
			: mLoaded(false), mHandle(0), mGetState(0), mSetState(0)
		{
		}

		~XInputLoader()
		{
		}

		void ensureLoaded()
		{
			if (!mLoaded)
			{
				// Keep hold of error codes from each library we try to load.
				DWORD xinput1_3_ErrorCode = 0;
				DWORD xinput1_4_ErrorCode = 0;
				DWORD xinput9_1_ErrorCode = 0;

				// Try to load the latest XInput DLL available
				
				// XInput 1.4: ships with Windows 8, 8.1 and 10
				mHandle = LoadLibrary(L"xinput1_4.dll");
				xinput1_4_ErrorCode = GetLastError();
				mLibVersion = LIBVER_xinput1_4;

				// XInput 1.3: ships with the DirectX End-User Runtimes (June 2010) and supports older versions of Windows
				if (mHandle == NULL)
				{
					mHandle = LoadLibrary(L"xinput1_3.dll");
					xinput1_3_ErrorCode = GetLastError();
					mLibVersion = LIBVER_xinput1_3;
				}

				// XInput 9.1.0: ships with Windows Vista and 7 (and 8, but we prefer the latest version)
				if (mHandle == NULL)
				{
					mHandle = LoadLibrary(L"xinput9_1_0.dll");
					xinput9_1_ErrorCode = GetLastError();
					mLibVersion = LIBVER_xinput9_1_0;
				}

				if (mHandle != NULL)
				{
					// Ordinal 100 is the same as XInputGetState but supports the Guide button.
					mGetState = (XInputGetStatePointer)GetProcAddress(mHandle, (LPCSTR)100);
					mGuideSupported = mGetState != NULL;
					// Use the standard XInputGetState function in case the enhanced version is not available
					if (mGetState == NULL)
					{
						mGetState = (XInputGetStatePointer)GetProcAddress(mHandle, "XInputGetState");
					}

					mSetState = (XInputSetStatePointer)GetProcAddress(mHandle, "XInputSetState");
					
					mLoaded = true;
				}
				else
				{
					printf_s("Failed to load xinput1_3.dll, xinput1_4.dll and xinput9_1_0.dll (error codes 0x%08x, 0x%08x, 0x%08x respectively; check that \"DirectX End-User Runtimes (June 2010)\""
						" is installed (http://www.microsoft.com/en-us/download/details.aspx?id=8109)\n", xinput1_3_ErrorCode, xinput1_4_ErrorCode, xinput9_1_ErrorCode);
					mLibVersion = LIBVER_none;
				}
			}
		}

		XInputGetStatePointer mGetState;
		XInputSetStatePointer mSetState;
		libver mLibVersion;
		bool mGuideSupported;

	private:
		bool mLoaded;
		HMODULE mHandle;
	};

	XInputLoader gXInputLoader;
}

DWORD XInputGamePadGetState(DWORD dwUserIndex, XINPUT_STATE* pState)
{
	gXInputLoader.ensureLoaded();
	if (gXInputLoader.mGetState != NULL)
	{
		return gXInputLoader.mGetState(dwUserIndex, pState);
	}
	else
	{
		return ERROR_DEVICE_NOT_CONNECTED;
	}
}

DWORD XInputGamePadSetState(DWORD dwUserIndex, XINPUT_VIBRATION *vibration)
{
	gXInputLoader.ensureLoaded();
	if (gXInputLoader.mSetState != NULL)
	{
		return gXInputLoader.mSetState(dwUserIndex, vibration);
	}
	else
	{
		return ERROR_DEVICE_NOT_CONNECTED;
	}
}

JNIEXPORT jint JNICALL Java_com_ivan_xinput_natives_XInputNatives_getLoadedLibVersion
  (JNIEnv *env, jclass cls)
{
	gXInputLoader.ensureLoaded();
	return gXInputLoader.mLibVersion;
}

JNIEXPORT jboolean JNICALL Java_com_ivan_xinput_natives_XInputNatives_isGuideButtonSupported
  (JNIEnv *env, jclass cls)
{
	gXInputLoader.ensureLoaded();
	return gXInputLoader.mGuideSupported;
}

JNIEXPORT jint JNICALL Java_com_ivan_xinput_natives_XInputNatives_pollDevice
  (JNIEnv *env, jclass cls, jint playerNum, jobject byteBuffer)
{
	// the byte buffer must be allocatedDirect(16)'d in Java...
	void *bbuf = env->GetDirectBufferAddress(byteBuffer);

	// ... because we're going to write straight into it
	XINPUT_STATE *state = (XINPUT_STATE *)bbuf;
	ZeroMemory(state, sizeof(XINPUT_STATE));

	return XInputGamePadGetState(playerNum, state);
}

JNIEXPORT jint JNICALL Java_com_ivan_xinput_natives_XInputNatives_setVibration
  (JNIEnv *env, jclass cls, jint playerNum, jint leftMotor, jint rightMotor)
{
	XINPUT_VIBRATION vib;
	vib.wLeftMotorSpeed = leftMotor & 0xFFFF;
	vib.wRightMotorSpeed = rightMotor & 0xFFFF;

	return XInputGamePadSetState(playerNum, &vib);
}