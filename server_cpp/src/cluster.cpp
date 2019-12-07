#include "cluster.grpc.pb.h"
#include "StubTaskManager.h"
#include "RpcServer.h"
#include "SlurmTaskManager.h"

int main(int argc, char* argv[])
{
	RpcServer server(std::make_unique<SlurmTaskManager>());
	server.Run(argc >= 2 ? argv[1] : "0.0.0.0:50051");
}
