#pragma once

class IFunctionLoader
{
	using Function = double(*)(double, int);
public:
	virtual Function GetFunction() = 0;
	virtual ~IFunctionLoader() = default;
private:
	void* m_handle = nullptr;
};
