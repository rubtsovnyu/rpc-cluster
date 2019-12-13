#include "SlurmTaskManager.h"
#include "Logger.h"

void SlurmTaskManager::NewTask(IOutputStream* stream, const std::string& functionName, boost::iterator_range<const double*> arguments)
{
	std::lock_guard<std::mutex> lock(m_queueSync);
	m_tasks.emplace(stream, functionName, arguments);
	cmd::log << "New task created" << std::endl;
	if (m_tasks.size() == 1)
	{
		m_tasks.front().StartAsync(OnTaskCompleted);
	}
}

cluster::PointBatch SlurmTaskManager::SuspendCurrentTask()
{
	std::lock_guard<std::mutex> lock(m_queueSync);
	return m_tasks.front().Suspend();
}

cluster::PointBatch SlurmTaskManager::ReadCurrentPoints()
{
	std::lock_guard<std::mutex> lock(m_queueSync);
	auto&& batch = m_tasks.front().Suspend();
	m_tasks.front().Resume();
    return std::move(batch);
}

void SlurmTaskManager::ResumeCurrentTask()
{
	std::lock_guard<std::mutex> lock(m_queueSync);
	m_tasks.front().Resume();
}

cluster::PointBatch SlurmTaskManager::TerminateCurrentTask()
{
	std::lock_guard<std::mutex> lock(m_queueSync);
	auto&& batch = m_tasks.front().Suspend();
	m_tasks.pop();
	return std::move(batch);
}
