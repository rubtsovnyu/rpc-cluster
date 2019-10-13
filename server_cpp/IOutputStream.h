#pragma once

class IOutputStream
{
public:
	virtual void operator<<(double value) = 0;
	virtual ~IOutputStream() = default;
};