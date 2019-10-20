#pragma once
#include "IOutputStream.h"
#include <queue>
#include <atomic>

class RpcPointsStream : public IOutputStream
{
public:
	RpcPointsStream();
	IOutputStream& operator<<(const double value) override;
	void CloseStream() override;
	bool IsClosed() const;
	bool Empty() const;
	double Pop();
private:
	std::queue<double> m_buffer;
	std::atomic_bool m_isClosed;
};

