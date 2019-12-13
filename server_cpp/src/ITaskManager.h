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
	virtual cluster::PointBatch SuspendCurrentTask() = 0;
	virtual cluster::PointBatch ReadCurrentPoints() = 0;
	virtual void ResumeCurrentTask() = 0;
	virtual cluster::PointBatch TerminateCurrentTask() = 0;
	virtual ~ITaskManager() = default;
};
