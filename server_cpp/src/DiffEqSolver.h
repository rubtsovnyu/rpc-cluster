#pragma once
#include <mutex>
#include "IOutputStream.h"
#include <atomic>

class DiffEqSolver
{
	using SolverStep = double(*)(double, int);
	enum
	{
		RESUME,
		SUSPEND,
		TERMINATE
	};
public:
	DiffEqSolver(SolverStep solverStep, IOutputStream* stream,
		double eps, double left, double right, double step);
	
	void Suspend();
	void Resume();
	void Terminate();
private:
	std::mutex m_mutex;
	std::condition_variable m_cv;
	std::atomic_uint8_t m_flag;
};

