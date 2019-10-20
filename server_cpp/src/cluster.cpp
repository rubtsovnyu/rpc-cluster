#include "cluster.grpc.pb.h"
#include "StubTaskManager.h"
#include "RpcServer.h"

int main()
{
	RpcServer server(std::make_unique<StubTaskManager>());
	server.Run("0.0.0.0:50051");
}
