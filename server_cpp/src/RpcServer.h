#pragma once
#include "cluster.grpc.pb.h"
#include "ControlServiceImpl.h"
#include <grpcpp/grpcpp.h>

class RpcServer
{
public:
	RpcServer(std::unique_ptr<ITaskManager>&& taskManager);
	void Run(const char* serverAddress);
	~RpcServer();
private:
	std::unique_ptr<grpc::ServerCompletionQueue> m_completionQueue;
	std::unique_ptr<grpc::Server> m_server;
	std::unique_ptr<ITaskManager> m_taskManager;
	ClusterService m_service;
};

