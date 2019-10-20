#include "StubTaskManager.h"
#include <thread>

void StubTaskManager::NewTask(IOutputStream* stream, const std::string& functionName,
	std::initializer_list<double>&& arguments)
{
	m_t = *arguments.begin();
	std::thread([this](IOutputStream* stream, double end, double step)
	{
		const int numSteps = floor((end - m_t) / step);
		for (int i = 0; i < numSteps; ++i)
		{
			m_x = sin(m_t);
			(*stream) << m_t << m_x;
			m_t = m_t + step;
			switch (m_flag)
			{
			case SUSPEND:
			{
				std::unique_lock<std::mutex> lock(m_mutex);
				m_cv.wait(lock);
			}
			break;
			case TERMINATE:
				return;
			default:
				break;
			}
		}
	}, stream, *(arguments.begin()+1), *(arguments.begin() + 2)).detach();
}

std::vector<double>&& StubTaskManager::SuspendCurrentTask()
{
	m_flag = SUSPEND;
	std::vector<double> vec{ m_t, m_x };
	return std::move(vec);
}

std::vector<double>&& StubTaskManager::ReadCurrentPoints()
{
	std::vector<double> vec{ m_t, m_x };
	return std::move(vec);
}

void StubTaskManager::ResumeCurrentTask()
{
	m_flag = RESUME;
	m_cv.notify_one();
}

std::vector<double>&& StubTaskManager::TerminateCurrentTask()
{
	m_flag = TERMINATE;
	std::vector<double> vec{ m_t, m_x };
	return std::move(vec);
}
