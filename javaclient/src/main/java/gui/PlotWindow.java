package gui;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.cluster.ControlServiceGrpc;
import io.grpc.cluster.Empty;
import io.grpc.cluster.PointBatch;
import io.grpc.cluster.RunMessage;
import io.grpc.stub.StreamObserver;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class PlotWindow {
    private static final Empty EMPTY_MESSAGE = Empty.newBuilder().build();

    private final Logger log = LoggerFactory.getLogger(PlotWindow.class);
    private final JFrame plotFrame;
    private final String functionName;
    private final Iterable<Double> params;
    private final Map<Integer, DataWrapper> dataWrapperMap;
    private final ReadWriteLock lock;

    private ControlServiceGrpc.ControlServiceStub serviceStub;
    private ControlServiceGrpc.ControlServiceBlockingStub blockingStub;
    private DataWrapper currentData;
    private XYSeriesCollection dataset;
    private AtomicInteger currentSize;
    private JSlider chartSlider;
    private JButton readCurrentBtn;
    private JButton resumeTaskBtn;
    private JButton pauseTaskBtn;
    private JButton stopTaskBtn;

    public PlotWindow(final String functionName,
                      final String serverAddress,
                      final int serverPort,
                      final Iterable<Double> params) throws HeadlessException {
        log.info("Creating new PlotWindow: {}", functionName);
        this.functionName = functionName;
        plotFrame = new JFrame("Plot: " + functionName);
        this.params = params;
        dataset = new XYSeriesCollection();
        dataWrapperMap = new ConcurrentHashMap<>();
        log.info("Dataset and colorList was initialized");
        currentSize = new AtomicInteger(-1);
        lock = new ReentrantReadWriteLock();
        initgRPC(serverAddress, serverPort);
        SwingUtilities.invokeLater(this::initGUI);
        startTask();
    }

    private void initGUI() {
        log.info("Creating GUI");
        plotFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        plotFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                gRPCterminateTask();
            }
        });

        JFreeChart chart = ChartFactory.createScatterPlot(functionName, "x", "y(x)", dataset);
        XYPlot chartPlot = (XYPlot) chart.getPlot();

        final var oldRenderer = chartPlot.getRenderer();
        final var toolTipGenerator = oldRenderer.getDefaultToolTipGenerator();
        final var urlGenerator = oldRenderer.getURLGenerator();

        final var renderer = new XYLineAndShapeRenderer(false, true) {
            @Override
            public Paint getItemPaint(int row, int column) {
                return new Color(currentData.getColors().get(column));
            }
        };
        renderer.setDefaultToolTipGenerator(toolTipGenerator);
        renderer.setURLGenerator(urlGenerator);

        chartPlot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);

        plotFrame.setLayout(new BorderLayout());
        plotFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        plotFrame.add(chartPanel, BorderLayout.CENTER);

        final var sliderLabel = new JLabel("Event: ");
        final var sliderValLabel = new JLabel("");

        chartSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
        chartSlider.addChangeListener(e -> {
            JSlider jSlider = (JSlider) e.getSource();
            final var currentZoomVal = jSlider.getValue();
            try {
                updateDataset(dataWrapperMap.get(currentZoomVal));
            } catch (IndexOutOfBoundsException ex) {
                log.error("CurrentZoomValue: {}, Size of data map: {}", currentZoomVal, dataWrapperMap.size());
            }
            chartPanel.revalidate();
            chartPanel.repaint();
            sliderValLabel.setText("" + currentZoomVal);
        });

        readCurrentBtn = new JButton("Read");
        resumeTaskBtn = new JButton("Resume");
        pauseTaskBtn = new JButton("Pause");
        stopTaskBtn = new JButton("Stop");

        switchButtonState(resumeTaskBtn);

        readCurrentBtn.addActionListener(e -> readCurrentPoints());
        resumeTaskBtn.addActionListener(e -> resumeTask());
        pauseTaskBtn.addActionListener(e -> pauseTask());
        stopTaskBtn.addActionListener(e -> stopTask());

        final var plotRpcBtnPanel = new JPanel();
        plotRpcBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        plotRpcBtnPanel.add(readCurrentBtn);
        plotRpcBtnPanel.add(resumeTaskBtn);
        plotRpcBtnPanel.add(pauseTaskBtn);
        plotRpcBtnPanel.add(stopTaskBtn);

        final var sliderAndButtonsPanel = new JPanel();
        sliderAndButtonsPanel.setLayout(new BoxLayout(sliderAndButtonsPanel, BoxLayout.Y_AXIS));
        sliderAndButtonsPanel.add(sliderLabel);
        sliderAndButtonsPanel.add(sliderValLabel);
        sliderAndButtonsPanel.add(chartSlider);
        sliderAndButtonsPanel.add(plotRpcBtnPanel);
        sliderAndButtonsPanel.setVisible(true);

        plotFrame.add(sliderAndButtonsPanel, BorderLayout.SOUTH);

        plotFrame.pack();
        plotFrame.setVisible(true);
        log.info("Frame created successfully!");
    }

    private void initgRPC(final String serverAddress, final int serverPort) {
        log.info("Initializing gRPC on {}:{}", serverAddress, serverPort);
        try {
            final var serverChannel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();
            this.serviceStub = ControlServiceGrpc.newStub(serverChannel);
//            this.blockingStub = ControlServiceGrpc.newBlockingStub(serverChannel); //For Igor's hotelka only
        } catch (StatusRuntimeException e) {
            log.error("gRPC init failed!", e);
            OptionsWindow.showErrorDialog("gRPC fail on init: " + e.getMessage());
        } finally {
            plotFrame.dispose();
        }
        log.info("gRPC initialized successfully");
    }

    private void startTask() {
        log.info("Starting a new task");
        RunMessage runMessage = RunMessage.newBuilder()
                .setFunctionName(functionName)
                .addAllParam(params)
                .build();
        log.info("Run message created");
        serviceStub.startTask(runMessage, new StreamObserver<>() {
            @Override
            public void onNext(PointBatch pointBatch) {
                EventQueue.invokeLater(() -> handlePointBatch(pointBatch));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("gRPC error on main stream: {}", throwable.getMessage(), throwable);
                disableAllButtons();
                plotFrame.dispose();
                OptionsWindow.showErrorDialog("Ошибка сервера: " + throwable.getCause().getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("Task done!");
            }
        });
        log.info("Task started");
    }

    public void readCurrentPoints() {
        log.info("Reading current points started");
        gRPCreadCurrentPoints();
    }

    private void gRPCreadCurrentPoints() {
//        final PointBatch pointBatch = blockingStub.readCurrentPoints(EMPTY_MESSAGE);
//        handlePointBatch(pointBatch);

        serviceStub.readCurrentPoints(EMPTY_MESSAGE, new StreamObserver<>() {
            @Override
            public void onNext(PointBatch pointBatch) {
                EventQueue.invokeLater(() -> handlePointBatch(pointBatch));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("gRPC error on read current points: {}", throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                log.info("gRPC read current completed");
            }
        });
    }

    public void resumeTask() {
        log.info("Resuming task started");
        switchButtonState(resumeTaskBtn);
        switchButtonState(pauseTaskBtn);

        gRPCresumeTask();
    }

    private void gRPCresumeTask() {
//        blockingStub.resumeTask(EMPTY_MESSAGE);

        serviceStub.resumeTask(EMPTY_MESSAGE, new StreamObserver<>() {
            @Override
            public void onNext(Empty empty) {
                log.info("Empty response on resuming task");
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("gRPC error on resume: {}", throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                log.info("gRPC resume completed");
            }
        });
    }

    public void pauseTask() {
        log.info("Pausing task started");
        switchButtonState(resumeTaskBtn);
        switchButtonState(pauseTaskBtn);

        gRPCsuspendTask();
//        refreshData(); //For test purposes only
    }

    private void gRPCsuspendTask() {
//        final PointBatch pointBatch = blockingStub.suspendTask(EMPTY_MESSAGE);
//        handlePointBatch(pointBatch);

        serviceStub.suspendTask(EMPTY_MESSAGE, new StreamObserver<>() {
            @Override
            public void onNext(PointBatch pointBatch) {
                EventQueue.invokeLater(() -> handlePointBatch(pointBatch));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("gRPC error on pause: {}", throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                log.info("gRPC pause completed");
            }
        });
    }

    public void stopTask() {
        log.info("Stopping task...");
        disableAllButtons();

        gRPCterminateTask();
    }

    private void gRPCterminateTask() {
//        final PointBatch pointBatch = blockingStub.terminateTask(EMPTY_MESSAGE);
//        handlePointBatch(pointBatch);

        serviceStub.terminateTask(EMPTY_MESSAGE, new StreamObserver<>() {
            @Override
            public void onNext(PointBatch pointBatch) {
                EventQueue.invokeLater(() -> handlePointBatch(pointBatch));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("gRPC error on stop: {}", throwable.getMessage(), throwable);
            }

            @Override
            public void onCompleted() {
                log.info("gRPC stop completed, task done");
            }
        });
    }

    private void disableAllButtons() {
        readCurrentBtn.setEnabled(false);
        resumeTaskBtn.setEnabled(false);
        pauseTaskBtn.setEnabled(false);
        stopTaskBtn.setEnabled(false);
    }

    private void handlePointBatch(PointBatch pointBatch) {
        log.info("New point batch. Size: {}", pointBatch.getPointList().size());
        DataWrapper dataWrapper = pointBatchToDataWrapper(pointBatch);
        addNewDataWrapper(dataWrapper);
    }

    private DataWrapper pointBatchToDataWrapper(final PointBatch pointBatch) {
        XYSeries xySeries = new XYSeries("Function");
        List<Integer> colors = new ArrayList<>(xySeries.getItemCount());
        pointBatch.getPointList().forEach(point -> {
            xySeries.add(point.getX(), point.getY());
            colors.add((int) point.getZ());
        });
        return new DataWrapper(xySeries, colors);
    }

    private void addNewDataWrapper(DataWrapper dataWrapper) {
        dataWrapperMap.put(currentSize.incrementAndGet(), dataWrapper);
        chartSlider.setMaximum(currentSize.get());
        chartSlider.setValue(currentSize.get());
    }

    private void updateDataset(final DataWrapper dataWrapper) {
        lock.writeLock().lock();
        try {
            currentData = dataWrapper;
            dataset.removeAllSeries();
            dataset.addSeries(dataWrapper.getXySeries());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void switchButtonState(final JButton button) {
        if (button.isEnabled()) {
            button.setEnabled(false);
        } else {
            button.setEnabled(true);
        }
    }

    /**
     * Generates 100 random points with random colors
     */
    private void refreshData() {
        XYSeries xySeries = new XYSeries("Function");
        List<Integer> colors = new ArrayList<>(xySeries.getItemCount());
        for (int i = 0; i < 100; i++) {
            xySeries.add(ThreadLocalRandom.current().nextDouble(10), ThreadLocalRandom.current().nextDouble(10));
            colors.add((int) (Math.random() * 0x1000000));
        }
        DataWrapper dataWrapper = new DataWrapper(xySeries, colors);
        addNewDataWrapper(dataWrapper);
    }

    private static class DataWrapper {
        private final XYSeries xySeries;
        private final List<Integer> colors;

        public DataWrapper(final XYSeries xySeries, final List<Integer> colors) {
            this.xySeries = xySeries;
            this.colors = colors;
        }

        public XYSeries getXySeries() {
            return xySeries;
        }

        public List<Integer> getColors() {
            return colors;
        }
    }
}
