#include "SlurmTask.h"

SlurmTask::SlurmTask(IOutputStream* stream, const std::string& pathToMath, boost::iterator_range<const double*> arguments)
	: m_pipe(m_ioService), m_stream(stream)
{
	m_readHandler = [this](const boost::system::error_code& e, std::size_t size)
	{
		if (e.failed() || size == 0)
			return;
		std::istream is(&m_buffer);
		std::string line;
		std::getline(is, line, '\n');
		std::stringstream ss(line);
		std::vector<Point> points;
		Point point{};
		while (ss >> point.x >> point.y >> point.color)
		{
			points.push_back(point);
		}
		(*m_stream) << points;
		boost::asio::async_read_until(m_pipe, m_buffer, '\n', m_readHandler);
	};
	
	std::ostringstream oss;
	if (!arguments.empty())
	{
		std::copy(arguments.begin(), arguments.end() - 1,
			std::ostream_iterator<int>(oss, " "));
		oss << arguments.back();
	}
	m_pathToBin = pathToMath + " " + oss.str();
}
