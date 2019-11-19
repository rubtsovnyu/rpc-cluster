package gui;

import io.grpc.StatusRuntimeException;
import io.grpc.cluster.ControlGrpc;
import io.grpc.cluster.Empty;
import io.grpc.cluster.OutputPointsStream;
import io.grpc.cluster.Points;
import io.grpc.cluster.RunMessage;
import io.grpc.stub.StreamObserver;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OptionsWindow extends JFrame {
    private static final Dimension textFieldDimension = new Dimension(500, 30);
    private static final Dimension tableDimension = new Dimension(500, 300);
    private final Logger log = LoggerFactory.getLogger(OptionsWindow.class);

    private final ControlGrpc.ControlStub controlServiceStub;
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

    public OptionsWindow(ControlGrpc.ControlStub controlServiceStub) throws HeadlessException {
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

        paramNumberSetBtn.addActionListener(e -> {
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
        });

        startServerBtn.addActionListener(e -> mathTaskThread.start());

        stopServerBtn.addActionListener(e -> {
            Empty empty = Empty.newBuilder().build();
            log.info("Stop performed!");
            controlServiceStub.terminateTask(empty, new StreamObserver<Points>() {
                @Override
                public void onNext(Points points) {
                    log.info("Last point got {}", points.toString());
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("Error during stop, {}", throwable.getMessage(), throwable);
                }

                @Override
                public void onCompleted() {
                    log.info("Stop completed!");
                }
            });
        });

        pauseServerBtn.addActionListener(e -> {
            Empty empty = Empty.newBuilder().build();
            log.info("Pause performed!");
            controlServiceStub.suspendTask(empty, new StreamObserver<Points>() {
                @Override
                public void onNext(Points points) {
                    log.info("Last point got {}", points.toString());
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("Error during pause, {}", throwable.getMessage(), throwable);
                }

                @Override
                public void onCompleted() {
                    log.info("Pause completed!");
                }
            });
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
            ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

            log.info("Enter to start task");

            //FOR TESTING !!!!
//            final var list = new ArrayList<Double>();
//            final var random = new Random();
//            for (int i = 0; i < 100; i++) {
//                list.add((double) random.nextInt(100) - 50);
//            }
//            final var outputPointsStreamIterator = list.iterator();

//            double[] initValues = {
//                    outputPointsStreamIterator.next().getValue(),
//                    outputPointsStreamIterator.next().getValue()
//            };

            final var xData = new CopyOnWriteArrayList<Double>();
            xData.add(0.0);
            final var yData = new CopyOnWriteArrayList<Double>();
            yData.add(0.0);

            log.info("After 2 init values sizes: {} and {}", xData.size(), yData.size());



            XYChart xyChart = QuickChart.getChart(functionNameFiled.getText(),
                    "x", "y", "y(x)", xData, yData);

            JPanel XYChartPanel = new XChartPanel<XYChart>(xyChart);

            JFrame XYChartFrame = new JFrame("Chart");
            XYChartFrame.setLayout(new BorderLayout());
            XYChartFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            XYChartFrame.add(XYChartPanel, BorderLayout.CENTER);

            final var sliderLabel = new JLabel("Val: ");
            final var sliderValLabel = new JLabel("");
            final var sliderPanel = new JPanel();
            sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
            sliderPanel.add(sliderLabel);
            sliderPanel.add(sliderValLabel);

            final var paramIter = getParamsFromFields().iterator();
            paramIter.next();
            final var startX = paramIter.next();
            final var endX = paramIter.next();
            final var step = paramIter.next();
            final var numberOfSteps = (int) Math.round((endX - startX) / step);
            log.info("Number of steps: {}", numberOfSteps);
            final var chartSlider = new JSlider(JSlider.HORIZONTAL, 1, numberOfSteps, numberOfSteps);
            chartSlider.addChangeListener(e -> {
                JSlider jSlider = (JSlider) e.getSource();
                final var currentZoomVal = jSlider.getValue();
                try {
                    xyChart.updateXYSeries("y(x)", xData.subList(0, currentZoomVal), yData.subList(0, currentZoomVal), null);
                } catch (IndexOutOfBoundsException ex) {
                    log.info("CurrentZoomValue: {}, Max Array Size: {} and {}", currentZoomVal, xData.size(), yData.size());
                }
                XYChartPanel.revalidate();
                XYChartPanel.repaint();
                sliderValLabel.setText("" + currentZoomVal);
            });

            sliderPanel.add(chartSlider);

            sliderPanel.setVisible(true);

            XYChartFrame.add(sliderPanel, BorderLayout.SOUTH);

            XYChartFrame.pack();
            XYChartFrame.setVisible(true);

            final var xOrY = new AtomicBoolean(); // false = x, true = y

//            final var sw = new SwingWrapper<XYChart>(xyChart);
//            sw.displayChart();

            controlServiceStub.startTask(runMessage, new StreamObserver<OutputPointsStream>() {
                @Override
                public void onNext(OutputPointsStream outputPointsStream) {
                    readWriteLock.writeLock().lock();
                    try {
                        if (xOrY.get()) {
                            yData.add(outputPointsStream.getValue());
                            xOrY.set(false);
                            try {
                                readWriteLock.readLock().lock();
                                try {
                                    xyChart.updateXYSeries("y(x)", xData, yData, null);
                                } finally {
                                    readWriteLock.readLock().unlock();
                                }
                            } catch (Exception e) {
                                log.error("xData size: {}, yData size: {}", xData.size(), yData.size(), e);
                            }
                            XYChartPanel.revalidate();
                            XYChartPanel.repaint();
                        } else {
                            xData.add(outputPointsStream.getValue());
                            xOrY.set(true);
                        }
                    } finally {
                        readWriteLock.writeLock().unlock();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("gRPC error: {}", throwable.getMessage(), throwable);
                }

                @Override
                public void onCompleted() {
                    log.info("Stream ended! Kaef");
                }
            });

//            sliderPanel.setVisible(true);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {}", e.getStatus());
        }
    }

    private boolean checkValidity() {
        boolean flag;

        functionNameFiled.setEditable(false);
        epsPanel.getjTextField().setEditable(false);
        startXPanel.getjTextField().setEditable(false);
        endXPanel.getjTextField().setEditable(false);
        stepPanel.getjTextField().setEditable(false);

        if (functionNameFiled.getText().isEmpty()) {
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
