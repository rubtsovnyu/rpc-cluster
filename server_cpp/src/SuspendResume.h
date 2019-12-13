#pragma once
#include <boost/process.hpp>

long SuspendProcess(boost::process::child& process);
long ResumeProcess(boost::process::child& process);