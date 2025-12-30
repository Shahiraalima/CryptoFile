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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DecryptPopupController {
    @FXML private ListView<File> listView;
    @FXML private Button closeButton;

    private Label listFileName, listFileSize, greenTick, fileIcon, timeNeededLabel;
    private HBox fileNameHBox, nameAndTick, row;
    private VBox middleVbox;
    private ProgressBar progressBar;
    private Button deleteButton;

    private Stage stage;
    private boolean encComplete = false;
    private final Map<File, ProgressBar> progressBarMap = new HashMap<>();
    private final Map<File, Label> greenTickMap = new HashMap<>();
    private final Map<File, Label> timeLabelMap = new HashMap<>();


    // Method to load data into the ListView and start encryption
    public void loadData(ObservableList<File> fileList, String password) {
        listView.setItems(fileList);
        customLIstView();
        performDecryption(fileList, password);
    }

    // Method to perform encryption in background parallelly
    private void performDecryption(ObservableList<File> fileList, String password) {
        Task<Void> encTask = new Task<>() {

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
                        logInfo.setAction("decrypt");

                        boolean flag = false;
                        try {
                            if (isCancelled()) {
                                logInfo.setStatus("cancelled");
                                LogDAO.logActivity(logInfo);
                                return;
                            }

                            String inputFile = file.getAbsolutePath();
                            String outputFile = inputFile + ".dec";

                            FileDAO fileDAO = new FileDAO();
                            String fileHash = fileDAO.getFileHash(file);
                            boolean fileEncrypted = fileDAO.validityEncryption(fileHash, SessionManager.loggedInUser.getUser_id());
                            if(!fileEncrypted) {
                                Platform.runLater(() -> {
                                    Label alertLabel = new Label("File " + file.getName() + " is not encrypted by this application");
                                    Shared.showAlert(alertLabel);
                                });
                                logInfo.setStatus("failed");
                                LogDAO.logActivity(logInfo);
                                latch.countDown();
                                return;
                            }

                            long startTime = System.nanoTime();

                            EncryptAndDecryptUtil.decryptFile(inputFile, outputFile, password, progress -> {
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

                            FileInfo fileInfo = new FileInfo();
                            fileInfo.setUser_id(SessionManager.loggedInUser.getUser_id());
                            fileInfo.setOg_file_hash(fileHash);
                            fileInfo.setEncrypted_file_name(outputFile.substring(outputFile.lastIndexOf(File.separator) + 1));
                            fileInfo.setEncrypted_file_size((long) new File(outputFile).length());

                            fileDAO.updateForDecryption(fileInfo);

                            long endTime = System.nanoTime();
                            long millisecondsTaken = (endTime - startTime) / 1_000_000;
                            String timeText = String.format("Time: %.2f ms", (double) millisecondsTaken); // TODO: improve time format


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

        encTask.setOnSucceeded(event -> onDecryptionComplete());

        encTask.setOnFailed(event -> {
            Label alertLabel = new Label("Encryption process failed");
            Shared.showAlert(alertLabel);
            onDecryptionComplete();
        });

        new Thread(encTask).start();
    }

    // Method called when encryption is complete
    private void onDecryptionComplete() {
        encComplete = true;
        stage.setOnCloseRequest(null);
        closeButton.setOnAction(event -> stage.close());
    }

    // Handle close button action
    public void handleCloseButton() {
        if (!encComplete) {
            Label alertLabel = new Label("Please wait until decryption is complete");
            Shared.showAlert(alertLabel);
        } else {
            stage.close();
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
                    listFileSize = new Label("(" + item.length() / 1024 + " KB)"); //TODO: create function for file size

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


                    setGraphic(row);

                    deleteButton.setOnAction(e -> {
                        //TODO: clicking on the delete button will stop any progress for the file. not removing the file from the list crossover the file
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

//TODO: add cancel button for each file to stop encryption of that particular file
//TODO: cancel all button to stop encryption of all files

