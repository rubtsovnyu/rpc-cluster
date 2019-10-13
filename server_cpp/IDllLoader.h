#pragma once
#include <string>

class IDllLoader
{
	using Function = void(*)();
public:
	virtual Function GetFunction(const std::string& functionName) = 0;
	virtual ~IDllLoader() = default;
private:
	void* m_handle = nullptr;
};
