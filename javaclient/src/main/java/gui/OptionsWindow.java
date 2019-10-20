package gui;

import io.grpc.cluster.ControlGrpc;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OptionsWindow extends JFrame {

    private final ControlGrpc.ControlStub controlServiceStub;
    private JTable paramTable;
    private JScrollPane paramTableScrollPane;

    public OptionsWindow(ControlGrpc.ControlStub controlServiceStub) throws HeadlessException {
        super("Параметры функции");
        this.controlServiceStub = controlServiceStub;
        init();
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final var contentPane = getContentPane();
        final var optionsLayout = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
        contentPane.setLayout(optionsLayout);

        final var functionLabel = new JLabel("Ваша функция:");
        functionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final var functionNameFiled = new JTextField();
        functionNameFiled.setAlignmentX(Component.LEFT_ALIGNMENT);

        final var functionNamePanel = new JPanel();
        functionNamePanel.setLayout(new BoxLayout(functionNamePanel, BoxLayout.X_AXIS));
        functionNamePanel.add(functionLabel);
        functionNamePanel.add(functionNameFiled);
        contentPane.add(functionNamePanel);

        final var paramLabel = new JLabel("Количество параметров:");
        paramLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final var paramNumberField = new JTextField();
        final var paramNumberDoc = (PlainDocument) paramNumberField.getDocument();
        paramNumberDoc.setDocumentFilter(new InputUtils.DigitFilter());
        paramNumberField.setAlignmentX(Component.LEFT_ALIGNMENT);

        final var paramNumberSetBtn = new JButton("Set");

        final var paramPanel = new JPanel();
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.X_AXIS));
        paramPanel.add(paramLabel);
        paramPanel.add(paramNumberField);
        paramPanel.add(paramNumberSetBtn);
        contentPane.add(paramPanel);

        final var paramTablePanel = new JPanel();
        paramTablePanel.setLayout(new BoxLayout(paramTablePanel, BoxLayout.X_AXIS));
        contentPane.add(paramTablePanel);

        final var startServerBtn = new JButton("Start");
        final var readServerBtn = new JButton("Read");
        final var resumeServerBtn = new JButton("Resume");
        final var pauseServerBtn = new JButton("Pause");
        final var stopServerBtn = new JButton("Stop");

        final var serverButtonsPanel = new JPanel();
        serverButtonsPanel.setLayout(new BoxLayout(serverButtonsPanel, BoxLayout.X_AXIS));
        serverButtonsPanel.add(startServerBtn);
        serverButtonsPanel.add(readServerBtn);
        serverButtonsPanel.add(resumeServerBtn);
        serverButtonsPanel.add(pauseServerBtn);
        serverButtonsPanel.add(stopServerBtn);
        contentPane.add(serverButtonsPanel);

        setPreferredSize(new Dimension(600, 600));

        paramNumberSetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (paramTableScrollPane != null) {
                    paramTablePanel.remove(paramTableScrollPane);
                }
                paramTableScrollPane = updateParams(Integer.parseInt(paramNumberField.getText()));
                paramTable = (JTable) paramTableScrollPane.getViewport().getView();
                paramTablePanel.add(paramTableScrollPane);
                validate();
                repaint();
            }
        });

        pack();
        setVisible(true);
    }

    private JScrollPane updateParams(final int paramNumber) {
        final var paramsTable = new JTable(paramNumber + 1, 2);
        paramsTable.getColumnModel().getColumn(0).setHeaderValue("Название переменной");
        paramsTable.getColumnModel().getColumn(0).setMinWidth(150);
        paramsTable.getColumnModel().getColumn(1).setHeaderValue("Значение");
        paramsTable.getColumnModel().getColumn(1).setMinWidth(300);
        JScrollPane jScrollPane = new JScrollPane(paramsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paramsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jScrollPane.getComponent(0);
        return jScrollPane;
    }


}
