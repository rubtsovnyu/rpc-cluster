#include "DiffEqSolver.h"

DiffEqSolver::DiffEqSolver(SolverStep solverStep, IOutputStream* stream,
	double eps, double left, double right, double step)
	: m_flag(0)
{
	std::thread([this](SolverStep solver, IOutputStream* stream, 
		double eps, double left, double right, double step)
	{
		for (int i = 0; i < floor((right - left) / step); ++i)
		{
			double sum = 1, q = 1;
			for (int n = 1; fabs(q) >= eps; n++)
			{
				q *= solver(left, n);
				sum += q;
				switch (m_flag)
				{
				case SUSPEND:
				{
					(*stream) << left << sum;
					std::unique_lock<std::mutex> lock(m_mutex);
					m_cv.wait(lock);
				}
					break;
				case TERMINATE:
					return 0;
				default:
					break;
				}
			}
			left += step;
			(*stream) << left << sum;
		}
		return 0;
	}, solverStep, stream, eps, left, right, step).detach();
}

void DiffEqSolver::Suspend()
{
	m_flag = SUSPEND;
}

void DiffEqSolver::Resume()
{
	m_flag = RESUME;
	m_cv.notify_one();
}

void DiffEqSolver::Terminate()
{
	m_flag = TERMINATE;
}
