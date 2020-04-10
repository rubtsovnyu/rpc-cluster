import gui.OptionsWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class grpcClient {
    private static final Logger logger = LoggerFactory.getLogger(grpcClient.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.error("No arguments found. Pass [server_address] [server_port] params.");
            throw new IllegalArgumentException("Wrong args!");
        }

        final var serverAddress = args[0];

        JFrame.setDefaultLookAndFeelDecorated(true);
        UIManager.put("Table.gridColor", new ColorUIResource(Color.gray));

        final int serverPort;
        try {
            serverPort = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            logger.error("Wrong server port: {}", args[1], e);
            throw new IllegalArgumentException(e);
        }

        logger.info("Server address and port are good: {}:{}", serverAddress, serverPort);

        SwingUtilities.invokeLater(() -> new OptionsWindow());
    }
}
