#pragma once
#include "Point.h"
#include "IOutputStream.h"
#include <initializer_list>
#include <string>
#include <vector>

class ITaskManager
{
public:
	virtual void NewTask(IOutputStream* stream, 
		const std::string& functionName, const double* arguments) = 0;
	virtual Point SuspendCurrentTask() = 0;
	virtual Point ReadCurrentPoints() = 0;
	virtual void ResumeCurrentTask() = 0;
	virtual Point TerminateCurrentTask() = 0;
	virtual ~ITaskManager() = default;
};
