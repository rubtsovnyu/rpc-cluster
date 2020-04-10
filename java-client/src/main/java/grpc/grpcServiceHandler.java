package grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.cluster.ControlServiceGrpc;
import io.grpc.cluster.ControlServiceGrpc.ControlServiceStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class grpcServiceHandler {
    private final Logger logger = LoggerFactory.getLogger(grpcServiceHandler.class);
    private final String serviceAddress;
    private final int servicePort;

    private ControlServiceStub stub;

    public grpcServiceHandler(String serviceAddress, int servicePort) {
        this.serviceAddress = serviceAddress;
        this.servicePort = servicePort;
    }

    public ControlServiceStub getOrCreateStub() {
        if (stub != null) {
            createStub();
        }
        return stub;
    }

    private void createStub() {
        logger.info("Creating service stub for {}:{}", serviceAddress, servicePort);

        ManagedChannel serviceChannel = ManagedChannelBuilder
                .forAddress(serviceAddress, servicePort)
                .usePlaintext()
                .build();
        logger.info("Managed channel created");

        stub = ControlServiceGrpc.newStub(serviceChannel);
        logger.info("Stub created successfully");
    }
}
