package gui;

import function.ParamTableWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;

public class OptionsWindow extends JFrame {
    private static final Dimension TEXT_FIELD_DIMENSION = new Dimension(500, 30);
    private static final Dimension TABLE_DIMENSION = new Dimension(500, 300);

    private final Logger logger = LoggerFactory.getLogger(OptionsWindow.class);
    private final String FUNCTION_PATH_PARAM_DESC = "Function executable path:";
    private final String FUNCTION_PARAM_NUMBER_DESC = "Количество параметров:";

    private JTextField functionNameFiled;
    private JTextField paramNumberField;
    private ParamTableWrapper paramTableWrapper;

    private int paramCount;

    public OptionsWindow() throws HeadlessException {
        super("Function parameters");
        initGUI();
        logger.info("Option window created");
    }

    static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void initGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final var contentPane = getContentPane();
        final var optionsLayout = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
        contentPane.setLayout(optionsLayout);

        contentPane.add(createFunctionNameSetJPanel());
        contentPane.add(createParamNumberSetJPanel());
        contentPane.add(createFunctionParamTableJPanel());

        final var startServerBtn = new JButton("Start");

        final var serverButtonsPanel = new JPanel();
        serverButtonsPanel.setLayout(new BoxLayout(serverButtonsPanel, BoxLayout.X_AXIS));
        serverButtonsPanel.add(startServerBtn);
        contentPane.add(serverButtonsPanel);


        startServerBtn.addActionListener(e -> startTask());

        setPreferredSize(new Dimension(600, 500));
        pack();
        setVisible(true);
    }

    private JPanel createFunctionNameSetJPanel() {
        final var functionLabel = new JLabel(FUNCTION_PATH_PARAM_DESC);
        functionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        functionNameFiled = new JTextField();
        functionNameFiled.setAlignmentX(Component.LEFT_ALIGNMENT);
        functionNameFiled.setMaximumSize(TEXT_FIELD_DIMENSION);

        final var functionNamePanel = new JPanel();
        functionNamePanel.setLayout(new BoxLayout(functionNamePanel, BoxLayout.X_AXIS));
        functionNamePanel.add(functionLabel);
        functionNamePanel.add(functionNameFiled);
        return functionNamePanel;
    }

    private JPanel createParamNumberSetJPanel() {
        final JLabel paramLabel = new JLabel(FUNCTION_PARAM_NUMBER_DESC);
        paramLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        paramNumberField = new JTextField();
        final PlainDocument paramNumberDoc = (PlainDocument) paramNumberField.getDocument();
        paramNumberDoc.setDocumentFilter(new InputUtils.DigitFilter());
        paramNumberField.setAlignmentX(Component.LEFT_ALIGNMENT);
        paramNumberField.setMaximumSize(TEXT_FIELD_DIMENSION);

        final var paramNumberSetBtn = new JButton("Set");
        paramNumberSetBtn.addActionListener(e -> {
            try {
                paramCount = Integer.parseInt(paramNumberField.getText());
            } catch (NumberFormatException ex) {
                showErrorDialog("Wrong param count");
                return;
            }
            createNew(paramTablePanel, paramCount);
        });

        final var paramPanel = new JPanel();
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.X_AXIS));
        paramPanel.add(paramLabel);
        paramPanel.add(paramNumberField);
        paramPanel.add(paramNumberSetBtn);
        return paramPanel;
    }

    private JPanel createFunctionParamTableJPanel() {
        final JPanel paramTablePanel = new JPanel();
        paramTablePanel.setLayout(new BoxLayout(paramTablePanel, BoxLayout.X_AXIS));
        paramTablePanel.setMaximumSize(TABLE_DIMENSION);
        JScrollPane jScrollPaneWithParamTable = createJScrollPaneWithParamTable();
        paramTablePanel.add(createJScrollPaneWithParamTable());
        paramTablePanel.add(jScrollPaneWithParamTable);
        return paramTablePanel;
    }

    private JScrollPane createJScrollPaneWithParamTable() {
        paramTableWrapper = new ParamTableWrapper(paramCount);
        JScrollPane jScrollPane = new JScrollPane(paramTableWrapper.getParamJTable(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return jScrollPane;
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

    private

    private void startTask() {
        if (!checkValidity()) {
            return;
        }
        logger.info("Validity check - ok");

        Iterable<Double> params;
        try {
            params = getParamsFromTable();
        } catch (IllegalArgumentException e) {
            return;
        }
        logger.info("Starting new task: {}", functionNameFiled.getText());
        SwingUtilities.invokeLater(() -> new PlotOptionsWindow(functionNameFiled.getText(), serverAddress, serverPort, params));
    }

    private Iterable<Double> getParamsFromTable() {

    }
}
