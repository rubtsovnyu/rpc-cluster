#pragma once
#include "IOutputStream.h"
#include <initializer_list>
#include <string>
#include <vector>

class ITaskManager
{
public:
	virtual void NewTask(IOutputStream* stream, 
		const std::string& functionName, const double* arguments) = 0;
	virtual std::vector<double>&& SuspendCurrentTask() = 0;
	virtual std::vector<double>&& ReadCurrentPoints() = 0;
	virtual void ResumeCurrentTask() = 0;
	virtual std::vector<double>&& TerminateCurrentTask() = 0;
	virtual ~ITaskManager() = default;
};
