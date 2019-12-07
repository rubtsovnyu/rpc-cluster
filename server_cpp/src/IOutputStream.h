#pragma once
#include <vector>
#include "Point.h"

class IOutputStream
{
public:
	virtual IOutputStream& operator<<(const std::vector<Point>& value) = 0;
	virtual void CloseStream() = 0;
	virtual ~IOutputStream() = default;
};
