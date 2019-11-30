#pragma once
#include "cluster.grpc.pb.h"
#include "IOutputStream.h"
#include <queue>
#include <atomic>
#include <condition_variable>
#include "Point.h"

class RpcPointsStream : public IOutputStream
{
public:
	RpcPointsStream();
	IOutputStream& operator<<(const std::vector<Point>&& value) override;
	void CloseStream() override;
	bool IsClosed() const;
	bool Empty() const;
	void WaitIfEmpty();
	cluster::PointBatch Pop();
private:
	std::queue<std::vector<Point>> m_buffer;
	std::atomic_bool m_isClosed;
	std::condition_variable m_event;
};

