#pragma once
#include <memory>
#include "IDllLoader.h"
#include <vector>
#include <atomic>
#include <thread>

class MathTask
{
public:
	void Start();
private:
	std::unique_ptr<IDllLoader> m_dll;
	std::vector<std::atomic<double>> m_volatilePoints;
};
