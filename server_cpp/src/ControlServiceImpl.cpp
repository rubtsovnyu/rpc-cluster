#include "ControlServiceImpl.h"

void ControlServiceImpl::SetTaskManager(ITaskManager* taskManager)
{
	m_taskManager = taskManager;
}

grpc::Status ControlServiceImpl::SuspendTask(grpc::ServerContext* context, const cluster::Empty* request,
	cluster::Points* response)
{
	const auto points{ m_taskManager->SuspendCurrentTask() };
	CopyPointsToResponse(response, points);
	return grpc::Status::OK;
}

grpc::Status ControlServiceImpl::ReadCurrentPoints(grpc::ServerContext* context, const cluster::Empty* request,
	cluster::Points* response)
{
	const auto points{ m_taskManager->ReadCurrentPoints() };
	CopyPointsToResponse(response, points);
	return grpc::Status::OK;
}

grpc::Status ControlServiceImpl::ResumeTask(grpc::ServerContext* context, const cluster::Empty* request,
	cluster::Empty* response)
{
	m_taskManager->ResumeCurrentTask();
	return grpc::Status::OK;
}

grpc::Status ControlServiceImpl::TerminateTask(grpc::ServerContext* context, const cluster::Empty* request,
	cluster::Points* response)
{
	const auto points{ m_taskManager->TerminateCurrentTask() };
	CopyPointsToResponse(response, points);
	return grpc::Status::OK;
}

void ControlServiceImpl::CopyPointsToResponse(cluster::Points* response, const std::vector<double>& points)
{
	for (int i = 0; i < points.size(); ++i)
	{
		response->set_value(i, points[i]);
	}
}
