#pragma once
#include "cluster.grpc.pb.h"
#include "IOutputStream.h"
#include <queue>
#include <atomic>
#include <condition_variable>

class RpcPointsStream : public IOutputStream
{
public:
	RpcPointsStream();
	IOutputStream& operator<<(const cluster::PointBatch& value) override;
	void CloseStream() override;
	bool IsClosed() const;
	bool Empty() const;
	void WaitIfEmpty();
	cluster::PointBatch Pop();
private:
	std::queue<cluster::PointBatch> m_buffer;
	std::atomic_bool m_isClosed;
	std::condition_variable m_event;
	std::mutex m_queueSync;
};

