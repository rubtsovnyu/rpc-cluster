#pragma once
#include "ITaskManager.h"
#include "SlurmTask.h"
#include <queue>
#include "cluster.pb.h"

class SlurmTaskManager : public ITaskManager
{
public:
	void NewTask(IOutputStream* stream,
		const std::string& functionName, boost::iterator_range<const double*> arguments) override;
    cluster::PointBatch SuspendCurrentTask() override;
    cluster::PointBatch ReadCurrentPoints() override;
	void ResumeCurrentTask() override;
    cluster::PointBatch TerminateCurrentTask() override;
private:
	std::queue<SlurmTask> m_tasks;
	std::mutex m_queueSync;
	std::function<void()> OnTaskCompleted = [this]()
	{
		std::lock_guard<std::mutex> lock(m_queueSync);
		m_tasks.pop();
		if (!m_tasks.empty())
		{
			m_tasks.front().StartAsync(OnTaskCompleted);
		}
	};
};

