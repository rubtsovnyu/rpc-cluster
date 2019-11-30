#include "StartTaskCall.h"

StartTaskCall::StartTaskCall(ClusterService* service,
	grpc::ServerCompletionQueue* completionQueue, ITaskManager* taskManager)
	: m_responder(&m_context), m_isFinished(false), m_isFirstCall(true),
m_service(service), m_completionQueue(completionQueue), m_taskManager(taskManager)
{
	m_service->RequestStartTask(&m_context, &m_request, &m_responder, m_completionQueue, m_completionQueue, this);
}


void StartTaskCall::Proceed()
{
	if (m_isFirstCall)
	{
		m_isFirstCall = false;
		new StartTaskCall(m_service, m_completionQueue, m_taskManager);
		m_taskManager->NewTask(&m_pointsStream, m_request.function_name(), m_request.param().data());
	}
	if (m_isFinished)
	{
		delete this;
		return;
	}
	if (m_pointsStream.IsClosed() && m_pointsStream.Empty())
	{
		m_isFinished = true;
		m_responder.Finish(grpc::Status::OK, this);
		return;
	}
	m_pointsStream.WaitIfEmpty();
	m_responder.Write(m_pointsStream.Pop(), this);
}
