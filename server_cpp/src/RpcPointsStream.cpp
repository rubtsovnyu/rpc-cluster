#include "RpcPointsStream.h"
#include "PointConverter.h"

RpcPointsStream::RpcPointsStream()
	: m_isClosed(false)
{
}

IOutputStream& RpcPointsStream::operator<<(const std::vector<Point>&& value)
{
	m_buffer.push(value);
	m_event.notify_one();
	return *this;
}

void RpcPointsStream::CloseStream()
{
	m_isClosed = true;
}

bool RpcPointsStream::IsClosed() const
{
	return m_isClosed;
}

bool RpcPointsStream::Empty() const
{
	return m_buffer.empty();
}

void RpcPointsStream::WaitIfEmpty()
{
	if (!Empty())
		return;
	std::mutex mutex;
	std::unique_lock<std::mutex> lock(mutex);
	m_event.wait(lock);
}

cluster::PointBatch RpcPointsStream::Pop()
{
	cluster::PointBatch batch;
	for (const auto& point : m_buffer.front())
	{
		*batch.add_point() = ConvertToRpcPoint(point);
	}
	m_buffer.pop();
	return batch;
}

