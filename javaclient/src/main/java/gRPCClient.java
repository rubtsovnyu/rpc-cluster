import com.google.common.base.Preconditions;
import gui.OptionsWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class gRPCClient {
    private static final Logger log = LoggerFactory.getLogger(gRPCClient.class);

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        UIManager.put("Table.gridColor", new ColorUIResource(Color.gray));

        Preconditions.checkArgument(args.length == 2, "Pass the params for client: [server address] [server port]");

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

        log.info("Server address and port are good: {}:{}", serverAddress, serverPort);

        SwingUtilities.invokeLater(() -> new OptionsWindow(serverAddress, serverPort));
    }
}
