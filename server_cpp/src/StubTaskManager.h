#pragma once
#include "ITaskManager.h"
#include <mutex>
#include <condition_variable>
#include <cmath>
#include <atomic>

class StubTaskManager final : public ITaskManager
{
public:
	void NewTask(IOutputStream* stream, const std::string& functionName, boost::iterator_range<const double*> arguments) override;
	Point SuspendCurrentTask() override;
	Point ReadCurrentPoints() override;
	void ResumeCurrentTask() override;
	Point TerminateCurrentTask() override;
private:
	enum
	{
		RESUME,
		SUSPEND,
		TERMINATE
	};
	
	IOutputStream* m_stream = nullptr;
	std::atomic<double> m_t = 0, m_x = 0;
	std::mutex m_mutex;
	std::condition_variable m_cv;
	std::atomic_uint8_t m_flag = RESUME;
};
