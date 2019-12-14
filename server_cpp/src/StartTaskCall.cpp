#include "StartTaskCall.h"
#include <boost/range.hpp>
#include <thread>
#include "Logger.h"

StartTaskCall::StartTaskCall(ClusterService* service,
	grpc::ServerCompletionQueue* completionQueue, ITaskManager* taskManager)
	: m_responder(&m_context), m_isFinished(false), m_isFirstCall(true),
m_service(service), m_completionQueue(completionQueue), m_taskManager(taskManager)
{
	m_service->RequestStartTask(&m_context, &m_request, &m_responder, m_completionQueue, m_completionQueue, this);
	cmd::log << "Created a new StartTaskCall" << std::endl;
}


void StartTaskCall::Proceed()
{
	cmd::log << "StartTask::Proceed called" << std::endl;
	if (m_isFirstCall)
	{
		cmd::log << "StartTask called" << std::endl;
		m_isFirstCall = false;
		new StartTaskCall(m_service, m_completionQueue, m_taskManager);
		m_taskManager->NewTask(&m_pointsStream, m_request.function_name(), 
			boost::make_iterator_range(m_request.param().begin(), m_request.param().end()));
	}
	if (m_isFinished)
	{
		delete this;
		cmd::log << "StartTask deleted" << std::endl;
		return;
	}
	if (m_pointsStream.IsClosed() && m_pointsStream.Empty())
	{
		m_isFinished = true;
		m_responder.Finish(grpc::Status::OK, this);
		cmd::log << "StartTask's stream is closed" << std::endl;
		return;
	}
	std::thread([this]()
	{
        m_pointsStream.WaitIfEmpty();
        m_responder.Write(m_pointsStream.Pop(), this);
        cmd::log << "StartTask's data is transmitted" << std::endl;
	}).detach();
}
