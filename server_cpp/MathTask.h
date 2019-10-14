#pragma once
#include <memory>
#include "IFunctionLoader.h"
#include "DiffEqSolver.h"

class MathTask
{
public:
	MathTask(std::unique_ptr<IFunctionLoader>&& dll, double eps, double left, double right, double step);
private:
	std::unique_ptr<IFunctionLoader> m_dll;
	DiffEqSolver m_solver;
};
