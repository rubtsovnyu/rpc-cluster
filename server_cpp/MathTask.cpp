#include "MathTask.h"

MathTask::MathTask(std::unique_ptr<IFunctionLoader>&& dll, double eps, double left, double right, double step)
	: m_dll(std::move(dll))
{
	m_solver(m_dll->GetFunction(),
}
