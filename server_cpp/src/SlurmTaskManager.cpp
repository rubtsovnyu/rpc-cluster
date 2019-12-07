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
