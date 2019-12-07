#pragma once
#include "Point.h"
#include "IOutputStream.h"
#include <string>
#include <boost/range.hpp>

class ITaskManager
{
public:
	virtual void NewTask(IOutputStream* stream, 
		const std::string& functionName, boost::iterator_range<const double*> arguments) = 0;
	virtual Point SuspendCurrentTask() = 0;
	virtual Point ReadCurrentPoints() = 0;
	virtual void ResumeCurrentTask() = 0;
	virtual Point TerminateCurrentTask() = 0;
	virtual ~ITaskManager() = default;
};
