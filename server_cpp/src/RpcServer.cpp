#include "RpcServer.h"
#include "StartTaskCall.h"
#include "Logger.h"

RpcServer::RpcServer(std::unique_ptr<ITaskManager>&& taskManager)
	: m_taskManager(std::move(taskManager))
{
	m_service.SetTaskManager(m_taskManager.get());
}

void RpcServer::Run(const char* serverAddress)
{
	const std::string address(serverAddress);
	grpc::ServerBuilder builder;
	builder.AddListeningPort(address, grpc::InsecureServerCredentials());
	builder.RegisterService(&m_service);
	m_completionQueue = builder.AddCompletionQueue();
	m_server = builder.BuildAndStart();
	cmd::log << "Server started up" << std::endl;
	new StartTaskCall(&m_service, m_completionQueue.get(), m_taskManager.get());
	void* tag;
	bool ok;
	for (;;)
	{
		GPR_ASSERT(m_completionQueue->Next(&tag, &ok));
		static_cast<StartTaskCall*>(tag)->Proceed();
	}
}

RpcServer::~RpcServer()
{
	m_server->Shutdown();
	m_completionQueue->Shutdown();
}
