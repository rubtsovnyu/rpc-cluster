import gui.OptionsWindow;
import io.grpc.ManagedChannelBuilder;
import io.grpc.cluster.ControlGrpc;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class gRPCClient {
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        UIManager.put("Table.gridColor", new ColorUIResource(Color.gray));
        final var serverAddress = args[0];
        if (serverAddress.isEmpty()) {
            throw new IllegalArgumentException("Wrong server address!");
        }
        final int serverPort;
        try {
            serverPort = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong server port: " + args[1]);
        }
        final var serverChannel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();
        final var serviceStub = ControlGrpc.newBlockingStub(serverChannel);
        SwingUtilities.invokeLater(() -> new OptionsWindow(serviceStub));
    }

}
