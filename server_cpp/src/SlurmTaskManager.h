#pragma once
#include "ITaskManager.h"
#include "SlurmTask.h"
#include <queue>

class SlurmTaskManager : public ITaskManager
{
public:
	void NewTask(IOutputStream* stream,
		const std::string& functionName, boost::iterator_range<const double*> arguments) override;
	Point SuspendCurrentTask() override { return {}; }
	Point ReadCurrentPoints() override  { return {}; }
	void ResumeCurrentTask() override  {}
	Point TerminateCurrentTask() override { return {}; }
private:
	std::queue<SlurmTask> m_tasks;
	std::mutex m_queueSync;
	std::function<void()> OnTaskCompleted = [this]()
	{
		m_tasks.pop();
		if (!m_tasks.empty())
		{
			m_tasks.front().StartAsync(OnTaskCompleted);
		}
	};
};

