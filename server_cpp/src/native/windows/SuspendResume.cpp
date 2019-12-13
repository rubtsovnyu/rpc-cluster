#if defined(_WIN32)

#include "SuspendResume.h"
#include <windows.h>
#include <Ntsecapi.h>

using pNtFunction = NTSTATUS(NTAPI *)(HANDLE ProcessHandle);

pNtFunction LoadFunction(const char* name)
{
	HMODULE hModule = GetModuleHandle(TEXT("ntdll.dll"));
	if (hModule == nullptr)
	{
		return nullptr;
	}
	return reinterpret_cast<pNtFunction>(GetProcAddress(hModule, name));
}

long SuspendProcess(boost::process::child& process)
{
	auto nativeSuspend = LoadFunction("NtSuspendProcess");
	return nativeSuspend ? nativeSuspend(process.native_handle()) : STATUS_INVALID_HANDLE;
}

long ResumeProcess(boost::process::child& process)
{
	auto nativeResume = LoadFunction("NtResumeProcess");
	return nativeResume ? nativeResume(process.native_handle()) : STATUS_INVALID_HANDLE;
}

#endif
