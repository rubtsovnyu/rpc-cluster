#pragma once
#include "ITaskManager.h"
#include <mutex>
#include <atomic>

class StubTaskManager final : public ITaskManager
{
public:
	void NewTask(IOutputStream* stream, const std::string& functionName, 
		std::initializer_list<double>&& arguments) override;
	std::vector<double>&& SuspendCurrentTask() override;
	std::vector<double>&& ReadCurrentPoints() override;
	void ResumeCurrentTask() override;
	std::vector<double>&& TerminateCurrentTask() override;
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
