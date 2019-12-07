#pragma once
#include <ctime>
#include <iomanip>
#include <iostream>

class Logger
{
public:
	std::ostream& operator<<(const char* message) const
	{
		std::time_t time = std::time(nullptr);
		std::tm tm = *std::localtime(&time);
		std::cout << "[" << std::put_time(&tm,"%d %b %T") << "] " << message;
		return std::cout;
	}
};

namespace cmd
{
	inline static const Logger log;
}
