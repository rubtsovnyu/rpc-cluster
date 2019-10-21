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
		m_taskManager->NewTask(&m_pointsStream, m_request.function_name(), 
			std::initializer_list<double>(m_request.arguments().data(), 
				m_request.arguments().data() + m_request.arguments().size()));
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
	cluster::OutputPointsStream reply;
	m_pointsStream.WaitIfEmpty();
	reply.set_value(m_pointsStream.Pop());
	m_responder.Write(reply, this);
}
