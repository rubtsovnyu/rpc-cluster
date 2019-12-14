package gui;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.cluster.ControlServiceGrpc;
import io.grpc.cluster.Empty;
import io.grpc.cluster.PointBatch;
import io.grpc.cluster.RunMessage;
import io.grpc.stub.StreamObserver;
import org.jzy3d.maths.Coord3d;
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
import java.util.concurrent.atomic.AtomicInteger;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class PlotOptionsWindow {
    private static final Empty EMPTY_MESSAGE = Empty.newBuilder().build();

    private final Logger log = LoggerFactory.getLogger(PlotOptionsWindow.class);
    private final JFrame plotManagerFrame;
    private final String functionName;
    private final Iterable<Double> params;
    private final Map<Integer, List<Coord3d>> coordMap;

    private ControlServiceGrpc.ControlServiceStub serviceStub;
    private AtomicInteger currentSize;
    private JSlider chartSlider;
    private JButton readCurrentBtn;
    private JButton resumeTaskBtn;
    private JButton pauseTaskBtn;
    private JButton stopTaskBtn;
    private PlotWindow plotWindow;

    public PlotOptionsWindow(final String functionName,
                             final String serverAddress,
                             final int serverPort,
                             final Iterable<Double> params) throws HeadlessException {
        log.info("Creating new PlotWindow: {}", functionName);
        this.functionName = functionName;
        plotManagerFrame = new JFrame("Plot: " + functionName);
        this.params = params;
        coordMap = new ConcurrentHashMap<>();
        log.info("Dataset and colorList was initialized");
        currentSize = new AtomicInteger(-1);
        initgRPC(serverAddress, serverPort);
        SwingUtilities.invokeLater(this::initGUI);
        startTask();
    }

    private void initGUI() {
        log.info("Creating GUI");
        plotManagerFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        plotManagerFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                gRPCterminateTask();
            }
        });
        plotWindow = new PlotWindow();
        EventQueue.invokeLater(() -> {
            try {
                plotWindow.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        final var sliderLabel = new JLabel("Event: ");
        final var sliderValLabel = new JLabel("");

        chartSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
        chartSlider.addChangeListener(e -> {
            JSlider jSlider = (JSlider) e.getSource();
            final var currentZoomVal = jSlider.getValue();
            try {
                EventQueue.invokeLater(() -> plotWindow.updateChart(coordMap.get(currentZoomVal)));
            } catch (IndexOutOfBoundsException ex) {
                log.error("CurrentZoomValue: {}, Size of data map: {}", currentZoomVal, coordMap.size());
            }
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

        plotManagerFrame.add(sliderAndButtonsPanel, BorderLayout.SOUTH);

        plotManagerFrame.pack();
        plotManagerFrame.setVisible(true);
        log.info("Frame created successfully!");
    }

    private void initgRPC(final String serverAddress, final int serverPort) {
        log.info("Initializing gRPC on {}:{}", serverAddress, serverPort);
        try {
            final var serverChannel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();
            this.serviceStub = ControlServiceGrpc.newStub(serverChannel);
        } catch (StatusRuntimeException e) {
            log.error("gRPC init failed!", e);
            OptionsWindow.showErrorDialog("gRPC fail on init: " + e.getMessage());
        } finally {
            plotManagerFrame.dispose();
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
                plotManagerFrame.dispose();
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
//        log.info("Pausing task started");
//        switchButtonState(resumeTaskBtn);
//        switchButtonState(pauseTaskBtn);
//
//        gRPCsuspendTask();
        EventQueue.invokeLater(this::refreshData); //For test purposes only
    }

    private void gRPCsuspendTask() {
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
        final List<Coord3d> coord3ds = pointBatchToCoord3ds(pointBatch);
        addNewCoord3ds(coord3ds);
    }

    private List<Coord3d> pointBatchToCoord3ds(final PointBatch pointBatch) {
        List<Coord3d> coord3ds = new ArrayList<>(pointBatch.getPointCount());
        pointBatch.getPointList().forEach(point -> coord3ds.add(new Coord3d(point.getX(),
                point.getY(),
                point.getZ())));
        return coord3ds;
    }

    private void addNewCoord3ds(final List<Coord3d> coord3ds) {
        coordMap.put(currentSize.incrementAndGet(), coord3ds);
        chartSlider.setMaximum(currentSize.get());
        chartSlider.setValue(currentSize.get());
    }

    private void switchButtonState(final JButton button) {
        if (button.isEnabled()) {
            button.setEnabled(false);
        } else {
            button.setEnabled(true);
        }
    }

    private void refreshData() {
        log.info("Refreshing data");
        int size = 100;
        List<Coord3d> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            points.add(new Coord3d(
                    (float) Math.random() - 0.5f,
                    (float) Math.random() - 0.5f,
                    (float) Math.random() - 0.5f));
        }
        plotWindow.updateChart(points);
        log.info("Data refreshed");
    }
}
