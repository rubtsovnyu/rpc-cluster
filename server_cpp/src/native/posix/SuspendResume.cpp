#if defined(__linux__)

#include "SuspendResume.h"
#include <signal.h>

long SuspendProcess(boost::process::child& process)
{
	return kill(process.native_handle(), SIGSTOP);
}

long ResumeProcess(boost::process::child& process)
{
	return kill(process.native_handle(), SIGCONT);
}

#endif
