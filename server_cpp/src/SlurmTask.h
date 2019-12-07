#pragma once
#include <boost/asio.hpp>
#include <boost/process.hpp>
#include "IOutputStream.h"
#include "ITaskManager.h"
#include "Logger.h"

class SlurmTask
{
public:
	SlurmTask(IOutputStream* stream, const std::string& pathToMath, boost::iterator_range<const double*> arguments);
	template<class Callable>
	void StartAsync(Callable&& callback)
	{
		boost::process::child cmd(
			boost::process::search_path(m_pathToBin),
			boost::process::std_out > m_pipe);
		cmd::log << "New task started up" << std::endl;
		boost::asio::async_read_until(m_pipe, m_buffer, '\n', m_readHandler);
		auto unused = std::async(std::launch::async, [this, &callback]()
		{
			m_ioService.run();
			callback();
		});
	}
private:
	boost::asio::streambuf m_buffer;
	boost::asio::io_service m_ioService;
	boost::process::async_pipe m_pipe;

	IOutputStream* m_stream;
	std::string m_pathToBin;
	std::function<void(const boost::system::error_code& e, std::size_t size)> m_readHandler;
};

