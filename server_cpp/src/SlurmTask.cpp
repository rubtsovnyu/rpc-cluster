#include "SlurmTask.h"
#include "SuspendResume.h"

SlurmTask::SlurmTask(IOutputStream* stream, const std::string& pathToMath, boost::iterator_range<const double*> arguments)
	: m_pipe(m_ioService), m_stream(stream)
{
	m_readHandler = [this](const boost::system::error_code& e, std::size_t size)
	{
		cmd::log << "ReadHandler called" << std::endl;
		if (e.failed() || size == 0)
		{
			cmd::log << "Reading completed" << std::endl;
			m_stream->CloseStream();
			return;
		}
		(*m_stream) << ReadPointsFromBuffer<true>();
		boost::asio::async_read_until(m_pipe, m_buffer, '\n', m_readHandler);
	};
	
	std::ostringstream oss;
	if (!arguments.empty())
	{
		std::copy(arguments.begin(), arguments.end() - 1,
			std::ostream_iterator<int>(oss, " "));
		oss << arguments.back();
	}
	m_runBinCommand = pathToMath + " " + oss.str();
	boost::trim(m_runBinCommand);
}

cluster::PointBatch SlurmTask::Suspend()
{
	cmd::log << "Suspend: " << SuspendProcess(m_process) << std::endl;
	return ReadPointsFromBuffer();
}

void SlurmTask::Resume()
{
	cmd::log << "Resume: " << ResumeProcess(m_process) << std::endl;
}

SlurmTask::~SlurmTask()
{
    m_process.terminate();
	m_stream->CloseStream();
}
