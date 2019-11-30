#pragma once
#include "cluster.grpc.pb.h"
#include "ITaskManager.h"

class ControlServiceImpl : public cluster::ControlService::Service
{
	// StartTask must be unimplemented
public:
	void SetTaskManager(ITaskManager* taskManager);
	grpc::Status SuspendTask(grpc::ServerContext* context, const cluster::Empty* request, cluster::Point* response)
		override;
	grpc::Status ReadCurrentPoints(grpc::ServerContext* context, const cluster::Empty* request,
		cluster::Point* response) override;
	grpc::Status ResumeTask(grpc::ServerContext* context, const cluster::Empty* request, cluster::Empty* response)
		override;
	grpc::Status TerminateTask(grpc::ServerContext* context, const cluster::Empty* request, cluster::Point* response)
		override;
	virtual ~ControlServiceImpl() = default;
private:
	ITaskManager* m_taskManager = nullptr;
};

using ClusterService = cluster::ControlService::WithAsyncMethod_StartTask<ControlServiceImpl>;

