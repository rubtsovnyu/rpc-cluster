#pragma once

class IOutputStream
{
public:
	virtual IOutputStream& operator<<(const double value) = 0;
	virtual ~IOutputStream() = default;
};