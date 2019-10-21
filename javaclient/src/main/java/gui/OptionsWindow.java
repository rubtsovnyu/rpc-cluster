package gui;

import io.grpc.StatusRuntimeException;
import io.grpc.cluster.ControlGrpc;
import io.grpc.cluster.OutputPointsStream;
import io.grpc.cluster.RunMessage;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

public class OptionsWindow extends JFrame {
    private static final Dimension textFieldDimension = new Dimension(500, 30);
    private static final Dimension tableDimension = new Dimension(500, 300);
//    private final Logger log =

    private final ControlGrpc.ControlBlockingStub controlServiceStub;
    private final MathTaskThread mathTaskThread;
    private JTable paramTable;
    private JScrollPane paramTableScrollPane;
    private JTextField functionNameFiled;
    private JTextField paramNumberField;

    private int paramCount;
    private InitParamPanelWrapper epsPanel;
    private InitParamPanelWrapper startXPanel;
    private InitParamPanelWrapper endXPanel;
    private InitParamPanelWrapper stepPanel;

    public OptionsWindow(ControlGrpc.ControlBlockingStub controlServiceStub) throws HeadlessException {
        super("Параметры функции");
        this.controlServiceStub = controlServiceStub;
        this.mathTaskThread = new MathTaskThread();
        initGUI();
        initRPC();
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

        epsPanel = new InitParamPanelWrapper("Точность");
        startXPanel = new InitParamPanelWrapper("Начальный X");
        endXPanel = new InitParamPanelWrapper("Конечный X");
        stepPanel = new InitParamPanelWrapper("Шаг");

        contentPane.add(epsPanel.getjPanel());
        contentPane.add(startXPanel.getjPanel());
        contentPane.add(endXPanel.getjPanel());
        contentPane.add(stepPanel.getjPanel());

        final var paramTablePanel = new JPanel();
        paramTablePanel.setLayout(new BoxLayout(paramTablePanel, BoxLayout.X_AXIS));
        paramTablePanel.setMaximumSize(tableDimension);
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

        paramNumberSetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    paramCount = Integer.parseInt(paramNumberField.getText());
                } catch (NumberFormatException ex) {
                    showErrorDialog("Неверное количество параметров");
                    return;
                }
                paramNumberField.setEditable(false);
                if (paramTableScrollPane != null) {
                    paramTablePanel.remove(paramTableScrollPane);
                }
                paramTableScrollPane = updateParams(paramCount);
                paramTable = (JTable) paramTableScrollPane.getViewport().getView();
                paramTablePanel.add(paramTableScrollPane);
                validate();
                repaint();
            }
        });

        startServerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mathTaskThread.start();
            }
        });

        setPreferredSize(new Dimension(600, 500));
        pack();
        setVisible(true);
    }

    private JScrollPane updateParams(final int paramNumber) {
        final var paramsTable = new JTable(paramNumber, 2);
        paramsTable.getColumnModel().getColumn(0).setHeaderValue("Название переменной");
        paramsTable.getColumnModel().getColumn(0).setMinWidth(150);
        paramsTable.getColumnModel().getColumn(1).setHeaderValue("Значение");
        paramsTable.getColumnModel().getColumn(1).setMinWidth(300);
        JScrollPane jScrollPane = new JScrollPane(paramsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paramsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        return jScrollPane;
    }

    private void initRPC() {

    }

    private void startTask() {
        if (!checkValidity()) {
            return;
        }
        RunMessage runMessage = RunMessage.newBuilder()
                .setFunctionName(functionNameFiled.getText())
                .addAllParams(getParamsFromFields())
                .build();

        try {
//            log.info("Enter to start task");
            Iterator<OutputPointsStream> outputPointsStreamIterator = controlServiceStub.startTask(runMessage);
//            log.info("Iterator got");

            //FOR TESTING !!!!
//            final var list = new ArrayList<Double>();
//            final var random = new Random();
//            for (int i = 0; i < 100; i++) {
//                list.add((double) random.nextInt(100) - 50);
//            }
//
//            final var outputPointsStreamIterator = list.iterator();

            double[] initValues = {
                    outputPointsStreamIterator.next().getValue(),
                    outputPointsStreamIterator.next().getValue()
            };

            final var xData = new ArrayList<Double>();
            xData.add(initValues[0]);
            final var yData = new ArrayList<Double>();
            yData.add(initValues[1]);

            XYChart xyChart = QuickChart.getChart(functionNameFiled.getText(),
                    "x", "y", "y(x)", xData, yData);

            final var sw = new SwingWrapper<XYChart>(xyChart);
            sw.displayChart();

            while (outputPointsStreamIterator.hasNext()) {
                xData.add(outputPointsStreamIterator.next().getValue());
                yData.add(outputPointsStreamIterator.next().getValue());

                //FOR TESTING
//                synchronized (Thread.currentThread()) {
//                    try {
//                        Thread.currentThread().wait(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }

                SwingUtilities.invokeLater(() -> {
                    xyChart.updateXYSeries("y(x)", xData, yData, null);
                    sw.repaintChart();
                });
            }
        } catch (StatusRuntimeException e) {
//            log.warn("RPC failed: {}", e.getStatus());
            return;
        }
    }

    private boolean checkValidity() {
        boolean flag = true;

        functionNameFiled.setEditable(false);
        epsPanel.getjTextField().setEditable(false);
        startXPanel.getjTextField().setEditable(false);
        endXPanel.getjTextField().setEditable(false);
        stepPanel.getjTextField().setEditable(false);

        if (functionNameFiled.getText().isEmpty()) {
            flag = false;
            showErrorDialog("Неверное имя функции");
            functionNameFiled.setEditable(true);
        }

        flag = checkNumberFieldValidity(epsPanel)
                && checkNumberFieldValidity(startXPanel)
                && checkNumberFieldValidity(endXPanel)
                && checkNumberFieldValidity(stepPanel);

        return flag;
    }

    private boolean checkNumberFieldValidity(InitParamPanelWrapper panel) {
        try {
            Double.parseDouble(panel.getjTextField().getText());
        } catch (NumberFormatException e) {
            showErrorDialog("Неверный параметр: " + panel.getjLabel().getText());
            panel.getjTextField().setEditable(true);
            return false;
        }
        return true;
    }

    private Iterable<Double> getParamsFromTable() {
        final var paramList = new ArrayList<Double>();
        for (int i = 0; i < paramCount; i++) {
            paramList.add(Double.parseDouble((String) paramTable.getValueAt(i, 1)));
        }
        return paramList;
    }

    private Iterable<Double> getParamsFromFields() {
        final var paramList = new ArrayList<Double>();
        paramList.add(Double.parseDouble(epsPanel.getjTextField().getText()));
        paramList.add(Double.parseDouble(startXPanel.getjTextField().getText()));
        paramList.add(Double.parseDouble(endXPanel.getjTextField().getText()));
        paramList.add(Double.parseDouble(stepPanel.getjTextField().getText()));
        return paramList;
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private static final class InitParamPanelWrapper {
        private JPanel jPanel;
        private JLabel jLabel;
        private JTextField jTextField;

        InitParamPanelWrapper(String title) {
            jPanel = new JPanel();
            jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));

            jLabel = new JLabel(title);
            jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            jTextField = new JTextField();
            jTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
            jTextField.setMaximumSize(textFieldDimension);

            jPanel.add(jLabel);
            jPanel.add(jTextField);
        }

        JPanel getjPanel() {
            return jPanel;
        }

        JLabel getjLabel() {
            return jLabel;
        }

        JTextField getjTextField() {
            return jTextField;
        }
    }

    private final class MathTaskThread extends Thread {
        MathTaskThread() {
            super("MathTask");
        }

        @Override
        public void run() {
            startTask();
        }
    }

}
