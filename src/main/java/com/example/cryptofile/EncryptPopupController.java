package com.example.cryptofile;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class EncryptPopupController {
    @FXML private ListView<File> listView;
    @FXML private Button closeButton;
    @FXML private Button cancelAllButton;
    @FXML private Label statusLabel;

    private Label listFileName, listFileSize, greenTick, fileIcon, timeNeededLabel;
    private HBox fileNameHBox, nameAndTick, row;
    private VBox middleVbox;
    private ProgressBar progressBar;
    private Button deleteButton;

    private Stage stage;
    private boolean encComplete = false;
    private final Map<File, Label> fileLabelMap = new ConcurrentHashMap<>();
    private final Map<File, ProgressBar> progressBarMap = new HashMap<>();
    private final Map<File, Label> greenTickMap = new HashMap<>();
    private final Map<File, Label> timeLabelMap = new HashMap<>();
    private final Map<File, AtomicBoolean> cancelFlagMap = new ConcurrentHashMap<>();
    private final Map<File, HBox> rowMap = new HashMap<>();

    private Task<Void> task;


    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failedCount = new AtomicInteger(0);
    private AtomicInteger cancelledCount = new AtomicInteger(0);


    // Method to load data into the ListView and start encryption
    public void loadData(ObservableList<File> fileList, String password) {
        listView.setItems(fileList);
        customLIstView();
        performEncryption(fileList, password);
    }

    // Method to perform encryption in background parallelly
    private void performEncryption(ObservableList<File> fileList, String password) {

        for (File file : fileList) {
            cancelFlagMap.put(file, new AtomicBoolean(false));
        }

        task = new Task<>() {
            private final Map<File, Long> lastUpdatedTime = new HashMap<>();
            private static final long UPDATE_INTERVAL = 10;
            private final int MAX_THREADS = Math.min(Runtime.getRuntime().availableProcessors(), fileList.size());


            @Override
            protected Void call() throws Exception {
                ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
                CountDownLatch latch = new CountDownLatch(fileList.size());

                for (File file : fileList) {
                    executor.submit(() -> {

                        LogInfo logInfo = new LogInfo();
                        logInfo.setUser_id(SessionManager.loggedInUser.getUser_id());
                        logInfo.setFile_name(file.getName());
                        logInfo.setFile_size(file.length());
                        logInfo.setAction("Encrypt");


                        boolean flag = false;
                        try {
                            if (isCancelled() || cancelFlagMap.get(file).get()) {
                                cancelledCount.getAndIncrement();
                                logInfo.setStatus("cancelled");
                                LogDAO.logCancelled(logInfo);
                                markFileCancelled(file);
                                latch.countDown();
                                return;
                            }

                            String inputFile = file.getAbsolutePath();
                            String outputFile = Shared.formatFileName(inputFile) + ".enc";

                            FileDAO fileDAO = new FileDAO();
                            String fileHash = fileDAO.getFileHash(file);
                            String fileEncrypted = fileDAO.checkFileExists(fileHash, SessionManager.loggedInUser.getUser_id());

                            if(fileEncrypted.equals("encrypted")) {
                                Platform.runLater(() -> {
                                    Label alertLabel = new Label("File " + file.getName() + " was already encrypted. Re-encrypting with new password. Old password will no longer work.");
                                    Shared.showAlert(alertLabel);
                                });
                            }

                            long startTime = System.nanoTime();

                            EncryptAndDecryptUtil.encryptFile(inputFile, outputFile, password, progress -> {
                                if (cancelFlagMap.get(file).get()) {
                                    return;
                                }

                                long currentTime = System.currentTimeMillis();
                                boolean shouldUpdate = false;

                                synchronized (lastUpdatedTime) {
                                    Long lastTime = lastUpdatedTime.get(file);
                                    if (lastTime == null || currentTime - lastTime >= UPDATE_INTERVAL || progress >= 1.0) {
                                        shouldUpdate = true;
                                        lastUpdatedTime.put(file, currentTime);
                                    }
                                }

                                if (shouldUpdate) {
                                    Platform.runLater(() -> {
                                        ProgressBar pb = progressBarMap.get(file);
                                        if (pb != null) {
                                            pb.setProgress(progress);
                                        }
                                    });
                                }
                            });

                            if(cancelFlagMap.get(file).get()) {
                                cancelledCount.getAndIncrement();
                                logInfo.setStatus("cancelled");
                                LogDAO.logCancelled(logInfo);
                                markFileCancelled(file);
                                latch.countDown();
                                return;
                            }

                            String fileHashAfter = fileDAO.getFileHash(new File(outputFile));

                            // After encryption, save file info to database
                            FileInfo fileInfo = new FileInfo();
                            fileInfo.setUser_id(SessionManager.loggedInUser.getUser_id());
                            fileInfo.setOg_file_name(file.getName());
                            fileInfo.setOg_file_size((long) file.length());
                            fileInfo.setOg_file_type(inputFile.substring(inputFile.lastIndexOf(".") + 1));
                            fileInfo.setOg_file_hash(fileHash);
                            String f = outputFile.substring(outputFile.lastIndexOf(File.separator) + 1);
                            fileInfo.setNew_file_name(Shared.formatFileName(f) + ".enc");
                            fileInfo.setNew_file_size((long) new File(outputFile).length());
                            fileInfo.setEncrypted_file_hash(fileHashAfter);

                            if(fileEncrypted.equals("not_found")) {
                                fileDAO.insertFile(fileInfo);
                            }
                            else {
                                fileDAO.updateForReencryption(fileInfo);
                            }

                            // Calculate time taken for encryption and update label
                            long endTime = System.nanoTime();
                            long millisecondsTaken = (endTime - startTime) / 1_000_000;
                            String timeText = Shared.formatTimeDuration(millisecondsTaken);

                            successCount.getAndIncrement();
                            logInfo.setStatus("success");
                            LogDAO.logSuccess(logInfo);

                            // Update time label on the JavaFX Application Thread
                            Platform.runLater(() -> {
                                Label timeLabel = timeLabelMap.get(file);
                                if (timeLabel != null) {
                                    timeLabel.setText(timeText);
                                }
                            });

                            flag = true;
                        } catch (Exception e) {
                            failedCount.getAndIncrement();
                            logInfo.setStatus("failed");
                            LogDAO.logFailure(logInfo);
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }

                        boolean finalFlag = flag;
                        Platform.runLater(() -> {
                            Label tick = greenTickMap.get(file);
                            if (tick != null && finalFlag) tick.setVisible(true);
                        });


                    });

                }
                latch.await();
                executor.shutdown();
                return null;
            }
        };

        task.setOnSucceeded(event -> onEncryptionComplete());

        task.setOnFailed(event -> {
            Label alertLabel = new Label("Encryption process failed");
            Shared.showAlert(alertLabel);
            onEncryptionComplete();
        });

        new Thread(task).start();
    }

    // Method called when encryption is complete
    private void onEncryptionComplete() {
        encComplete = true;
        stage.setOnCloseRequest(null);
        statusLabel.setText(String.valueOf(successCount) + " succeeded, " + String.valueOf(failedCount) + " failed, " + String.valueOf(cancelledCount) + " cancelled.");
        closeButton.setOnAction(event -> stage.close());
    }

    private void markFileCancelled(File file) {
        Platform.runLater(() -> {
            Label fileLabel = fileLabelMap.get(file);
            if (fileLabel != null) {
                fileLabel.setStyle("-fx-strikethrough: true");
            }
        });
    }

    // Handle close button action
    public void handleCloseButton() {
        if (!encComplete) {
            Label alertLabel = new Label("Please wait until encryption is complete");
            Shared.showAlert(alertLabel);
        } else {
            stage.close();
        }
    }

    @FXML
    public void handleCancelAllButton() {
        if(task != null && !task.isDone()) {
            task.cancel();
        }
        if (!encComplete) {
            for (File file : cancelFlagMap.keySet()) {
                cancelFlagMap.get(file).set(true);
                markFileCancelled(file);
            }
            cancelAllButton.setDisable(true);
            statusLabel.setText("All operations cancelled.");
            stage.setOnCloseRequest(null);
        }
    }

    // Custom ListView cell factory to display file details and progress
    private void customLIstView() {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    listFileName = new Label(item.getName());
                    listFileSize = new Label("(" + Shared.formatFileSize(item.length()) + ")");

                    timeNeededLabel = new Label();

                    greenTick = new Label();
                    greenTick.setGraphic(Shared.createIcon("greenTick"));

                    fileNameHBox = new HBox();
                    fileNameHBox.getChildren().addAll(listFileName, listFileSize);

                    nameAndTick = new HBox();
                    nameAndTick.getChildren().addAll(fileNameHBox, timeNeededLabel, greenTick);
                    HBox.setHgrow(fileNameHBox, Priority.ALWAYS);

                    progressBar = new ProgressBar();
                    progressBar.setMaxWidth(Double.MAX_VALUE);
                    progressBar.setProgress(0.0);


                    middleVbox = new VBox();
                    middleVbox.getChildren().addAll(nameAndTick, progressBar);

                    fileIcon = new Label();
                    fileIcon.setGraphic(Shared.createIcon("fileIcon"));

                    deleteButton = new Button();
                    deleteButton.setGraphic(Shared.createIcon("deleteButton"));

                    row = new HBox();
                    row.getChildren().addAll(fileIcon, middleVbox, deleteButton);
                    HBox.setHgrow(middleVbox, Priority.ALWAYS);

                    allinOne();

                    greenTick.setVisible(false);
                    greenTickMap.put(item, greenTick);
                    progressBarMap.put(item, progressBar);
                    timeLabelMap.put(item, timeNeededLabel);
                    rowMap.put(item, row);
                    fileLabelMap.put(item, listFileName);

                    setGraphic(row);

                    deleteButton.setOnAction(e -> {
                        if (!encComplete) {
                            cancelFlagMap.get(item).set(true);
                            markFileCancelled(item);
                            deleteButton.setDisable(true);
                        }
                    });
                }
            }
        });
    }

    // Method to add all style classes at once
    private void allinOne() {
        progressBar.getStyleClass().add("progressBar");
        greenTick.getStyleClass().add("greenTick");
        fileNameHBox.getStyleClass().add("fileNameHBox");
        middleVbox.getStyleClass().add("middleVbox");
        fileIcon.getStyleClass().add("fileIcon");
        deleteButton.getStyleClass().add("deleteButton");
        row.getStyleClass().add("row");
        timeNeededLabel.getStyleClass().add("timeNeededLabel");
    }

    // Setter for stage to manage close behavior
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
