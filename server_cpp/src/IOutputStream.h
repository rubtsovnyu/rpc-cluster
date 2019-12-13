#pragma once
#include "cluster.pb.h"

class IOutputStream
{
public:
	virtual IOutputStream& operator<<(const cluster::PointBatch& value) = 0;
	virtual void CloseStream() = 0;
	virtual ~IOutputStream() = default;
};
