#include "ControlServiceImpl.h"
#include "PointConverter.h"
#include "Logger.h"

void ControlServiceImpl::SetTaskManager(ITaskManager* taskManager)
{
	m_taskManager = taskManager;
}

grpc::Status ControlServiceImpl::SuspendTask(grpc::ServerContext* context, const cluster::Empty* request,
	cluster::Point* response)
{
	cmd::log << "SuspendTask called" << std::endl;
	*response = ConvertToRpcPoint(m_taskManager->SuspendCurrentTask());
	return grpc::Status::OK;
}

grpc::Status ControlServiceImpl::ReadCurrentPoints(grpc::ServerContext* context, const cluster::Empty* request,
	cluster::Point* response)
{
	cmd::log << "ReadCurrentPoints called" << std::endl;
	*response = ConvertToRpcPoint(m_taskManager->ReadCurrentPoints());
	return grpc::Status::OK;
}

grpc::Status ControlServiceImpl::ResumeTask(grpc::ServerContext* context, const cluster::Empty* request,
	cluster::Empty* response)
{
	cmd::log << "ResumeTask called" << std::endl;
	m_taskManager->ResumeCurrentTask();
	return grpc::Status::OK;
}

grpc::Status ControlServiceImpl::TerminateTask(grpc::ServerContext* context, const cluster::Empty* request,
	cluster::Point* response)
{
	cmd::log << "TerminateTask called" << std::endl;
	*response = ConvertToRpcPoint(m_taskManager->TerminateCurrentTask());
	return grpc::Status::OK;
}
