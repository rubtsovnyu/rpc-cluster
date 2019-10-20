#pragma once
#include "IOutputStream.h"
#include <ostream>

class OutputStreamAdapter : public IOutputStream
{
public:
	OutputStreamAdapter(std::ostream& stream) : m_stream(stream){}
	OutputStreamAdapter& operator<<(double value) override
	{
		m_stream << value;
		return *this;
	}
private:
	std::ostream& m_stream;
};
