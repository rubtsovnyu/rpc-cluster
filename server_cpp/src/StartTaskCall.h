#pragma once
#include "ControlServiceImpl.h"
#include "RpcPointsStream.h"

class StartTaskCall // TODO: timeout
{
public:
	StartTaskCall(ClusterService* service,
		grpc::ServerCompletionQueue* completionQueue, ITaskManager* taskManager);
	void Proceed();
private:
	grpc::ServerContext m_context;
	cluster::RunMessage m_request;
	grpc::ServerAsyncWriter<cluster::PointBatch> m_responder;
	
	bool m_isFinished;
	bool m_isFirstCall;
	RpcPointsStream m_pointsStream;
	
	// for creating new call
	ClusterService* m_service;
	grpc::ServerCompletionQueue* m_completionQueue;
	/////////////////////////

	ITaskManager* m_taskManager;
};
