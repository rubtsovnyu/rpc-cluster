#pragma once
#include "IOutputStream.h"
#include <ostream>

class OutputStreamAdapter : public IOutputStream
{
public:
	OutputStreamAdapter(std::ostream& stream) : m_stream(stream){}
	void operator<<(double value) override
	{
		m_stream << value;
	}
private:
	std::ostream& m_stream;
};
