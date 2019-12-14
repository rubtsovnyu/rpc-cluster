package gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.ArrayList;

public class OptionsWindow extends JFrame {
    private static final Dimension textFieldDimension = new Dimension(500, 30);
    private static final Dimension tableDimension = new Dimension(500, 300);

    private final Logger log = LoggerFactory.getLogger(OptionsWindow.class);
    private final String serverAddress;
    private final int serverPort;

    private JTable paramTable;
    private JScrollPane paramTableScrollPane;
    private JTextField functionNameFiled;
    private JTextField paramNumberField;
    private PlotOptionsWindow currentPlotWindow;

    private int paramCount;

    public OptionsWindow(final String serverAddress, final int serverPort) throws HeadlessException {
        super("Function param");
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        initGUI();
        log.info("OptionWindow created!");
    }

    static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void initGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final var contentPane = getContentPane();
        final var optionsLayout = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
        contentPane.setLayout(optionsLayout);

        final var functionLabel = new JLabel("Ваша функция:");
        functionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        functionNameFiled = new JTextField();
        functionNameFiled.setAlignmentX(Component.LEFT_ALIGNMENT);
        functionNameFiled.setMaximumSize(textFieldDimension);

        final var functionNamePanel = new JPanel();
        functionNamePanel.setLayout(new BoxLayout(functionNamePanel, BoxLayout.X_AXIS));
        functionNamePanel.add(functionLabel);
        functionNamePanel.add(functionNameFiled);
        contentPane.add(functionNamePanel);

        final var paramLabel = new JLabel("Количество параметров:");
        paramLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        paramNumberField = new JTextField();
        final var paramNumberDoc = (PlainDocument) paramNumberField.getDocument();
        paramNumberDoc.setDocumentFilter(new InputUtils.DigitFilter());
        paramNumberField.setAlignmentX(Component.LEFT_ALIGNMENT);
        paramNumberField.setMaximumSize(textFieldDimension);

        final var paramNumberSetBtn = new JButton("Set");

        final var paramPanel = new JPanel();
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.X_AXIS));
        paramPanel.add(paramLabel);
        paramPanel.add(paramNumberField);
        paramPanel.add(paramNumberSetBtn);
        contentPane.add(paramPanel);

        final var paramTablePanel = new JPanel();
        paramTablePanel.setLayout(new BoxLayout(paramTablePanel, BoxLayout.X_AXIS));
        paramTablePanel.setMaximumSize(tableDimension);
        updateParamTable(paramTablePanel, 0);
        contentPane.add(paramTablePanel);

        final var startServerBtn = new JButton("Start");

        final var serverButtonsPanel = new JPanel();
        serverButtonsPanel.setLayout(new BoxLayout(serverButtonsPanel, BoxLayout.X_AXIS));
        serverButtonsPanel.add(startServerBtn);
        contentPane.add(serverButtonsPanel);

        paramNumberSetBtn.addActionListener(e -> {
            try {
                paramCount = Integer.parseInt(paramNumberField.getText());
            } catch (NumberFormatException ex) {
                showErrorDialog("Неверное количество параметров");
                return;
            }
            if (paramTableScrollPane != null) {
                paramTablePanel.remove(paramTableScrollPane);
            }
            updateParamTable(paramTablePanel, paramCount);
        });

        startServerBtn.addActionListener(e -> startTask());

        setPreferredSize(new Dimension(600, 500));
        pack();
        setVisible(true);
    }

    private JScrollPane createTable(final int paramNumber) {
        final var paramsTable = new JTable(paramNumber, 2);
        paramsTable.getColumnModel().getColumn(0).setHeaderValue("Название (опционально)");
        paramsTable.getColumnModel().getColumn(0).setMinWidth(150);
        paramsTable.getColumnModel().getColumn(1).setHeaderValue("Значение");
        paramsTable.getColumnModel().getColumn(1).setMinWidth(300);
        JScrollPane jScrollPane = new JScrollPane(paramsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paramsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        return jScrollPane;
    }

    private void updateParamTable(JPanel paramTablePanel, int paramCount) {
        paramTableScrollPane = createTable(paramCount);
        paramTable = (JTable) paramTableScrollPane.getViewport().getView();
        paramTablePanel.add(paramTableScrollPane);
        validate();
        repaint();
    }

    private void startTask() {
        if (!checkValidity()) {
            return;
        }
        log.info("Validity check - ok");

        Iterable<Double> params;
        try {
            params = getParamsFromTable();
        } catch (IllegalArgumentException e) {
            return;
        }
        log.info("Starting new task: {}", functionNameFiled.getText());
        SwingUtilities.invokeLater(() -> new PlotOptionsWindow(functionNameFiled.getText(), serverAddress, serverPort, params));
    }

    private boolean checkValidity() {
        functionNameFiled.setEditable(false);

        if (functionNameFiled.getText().isEmpty()) {
            showErrorDialog("Неверное имя функции");
            functionNameFiled.setEditable(true);
            return false;
        }

        return true;
    }

    private Iterable<Double> getParamsFromTable() {
        final var paramList = new ArrayList<Double>();
        for (int i = 0; i < paramCount; i++) {
            try {
                paramList.add(Double.parseDouble((String) paramTable.getValueAt(i, 1)));
            } catch (NumberFormatException e) {
                showErrorDialog("Неверный параметр под номером: " + i);
                log.error("Invalid parameter in table on index {}", i, e);
                throw new IllegalArgumentException();
            } catch (NullPointerException e) {
                showErrorDialog("Параметр " + i + " не может быть пустым!");
                log.error("Empty parameter in table on index {}", i, e);
                throw new IllegalArgumentException();
            }
        }
        return paramList;
    }
}
