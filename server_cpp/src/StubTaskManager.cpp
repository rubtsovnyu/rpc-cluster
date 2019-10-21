#include "StubTaskManager.h"
#include <thread>

void StubTaskManager::NewTask(IOutputStream* stream, const std::string& functionName,
	std::initializer_list<double>&& arguments)
{
	m_t = *(arguments.begin()+1);
	std::thread([this](IOutputStream* stream, double sleep, double end, double step)
	{
		const int numSteps = floor((end - m_t) / step);
		for (int i = 0; i < numSteps; ++i)
		{
			m_x = sin(m_t);
			(*stream) << m_t << m_x;
			m_t = m_t + step;
			std::this_thread::sleep_for(std::chrono::milliseconds(static_cast<int>(sleep)));
			switch (m_flag)
			{
			case SUSPEND:
			{
				std::unique_lock<std::mutex> lock(m_mutex);
				m_cv.wait(lock);
			}
			break;
			case TERMINATE:
				stream->CloseStream();
				return;
			default:
				break;
			}
		}
		stream->CloseStream();
	}, stream, *arguments.begin(), *(arguments.begin() + 2), *(arguments.begin() + 3)).detach();
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
