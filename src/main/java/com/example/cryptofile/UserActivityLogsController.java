package com.example.cryptofile;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserActivityLogsController {

    @FXML private Label totalOperationsLabel;
    @FXML private Label encryptedCountLabel;
    @FXML private Label decryptedCountLabel;
    @FXML private Label successRateLabel;

    @FXML private TableView<LogInfo> activityTable;
    @FXML private TableColumn<LogInfo, String> statusColumn;
    @FXML private TableColumn<LogInfo, String> fileNameColumn;
    @FXML private TableColumn<LogInfo, String> operationColumn;
    @FXML private TableColumn<LogInfo, String> sizeColumn;
    @FXML private TableColumn<LogInfo, String> dateTimeColumn;
    @FXML private TableColumn<LogInfo, String> resultColumn;

    private LogDAO activityLogDAO = new LogDAO();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadActivityLogs();
        loadStatistics();
    }

    private void setupTableColumns() {
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            String icon = "success".equalsIgnoreCase(status) ? "✓" : "✗";
            return new SimpleStringProperty(icon);
        });
        statusColumn.setStyle("-fx-alignment: CENTER;");

        statusColumn.setCellFactory(column -> new TableCell<LogInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                }
            }
        });


        fileNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFile_name())
        );
        fileNameColumn.setStyle("-fx-alignment: CENTER;");

        operationColumn.setCellValueFactory(cellData -> {
            String operation = cellData.getValue().getAction();
            String display = formatOperation(operation);
            return new SimpleStringProperty(display);
        });
        operationColumn.setStyle("-fx-alignment: CENTER;");
        operationColumn.setCellFactory(column -> new TableCell<LogInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                }
            }
        });

        sizeColumn.setCellValueFactory(cellData -> {
                    long bytes = cellData.getValue().getFile_size();
                    String formattedSize = Shared.formatFIleSize(bytes);
                    return new SimpleStringProperty(formattedSize);
                });
        sizeColumn.setStyle("-fx-alignment: CENTER;");


        dateTimeColumn. setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            String formatted = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm: ss"));
            return new SimpleStringProperty(formatted);
        });
        dateTimeColumn.setStyle("-fx-alignment: CENTER;");

        // Result column
        resultColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().toUpperCase())
        );
        resultColumn.setStyle("-fx-alignment: CENTER;");
        resultColumn.setCellFactory(column -> new TableCell<LogInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                }
            }
        });

        activityTable.setPlaceholder(new Label("No activity logs found"));
    }


    private void loadActivityLogs() {
        Task<ObservableList<LogInfo>> loadTask = new Task<>() {
            @Override
            protected ObservableList<LogInfo> call() throws Exception {
                List<LogInfo> logs = LogDAO.getAllLogsByUserID(SessionManager.loggedInUser.getUser_id(), 100); // Last 100 logs
                return FXCollections.observableArrayList(logs);
            }
        };

        loadTask.setOnSucceeded(event -> {
            ObservableList<LogInfo> logs = loadTask.getValue();
            activityTable.setItems(logs);
            System.out.println("✓ Loaded " + logs.size() + " activity logs");
        });

        loadTask.setOnFailed(event -> {
            showError("Failed to load activity logs");
            event.getSource().getException().printStackTrace();
        });

        new Thread(loadTask, "load-logs-thread").start();
    }

    /**
     * Load statistics from database
     */
    private void loadStatistics() {
        Task<Void> statsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Get total operations count
                    int totalOperations = LogDAO.getTotalLogsCountByUserID(SessionManager.loggedInUser.getUser_id());

                    // Get encrypted files count
                    int encryptedCount = LogDAO.encryptedLogsCountByUserID(SessionManager.loggedInUser.getUser_id());

                    // Get decrypted files count
                    int decryptedCount = LogDAO.decryptedLogsCountByUserID(SessionManager.loggedInUser.getUser_id());

                    // Calculate success rate
                    double successRate = LogDAO.successRateByUserID(SessionManager.loggedInUser.getUser_id());

                    // Update UI on JavaFX thread
                    Platform. runLater(() -> {
                        totalOperationsLabel.setText(String.valueOf(totalOperations));
                        encryptedCountLabel.setText(String.valueOf(encryptedCount));
                        decryptedCountLabel.setText(String.valueOf(decryptedCount));
                        successRateLabel. setText(String.format("%.1f%%", successRate));

                        // Add animation effect (optional)
                        animateLabel(totalOperationsLabel);
                        animateLabel(encryptedCountLabel);
                        animateLabel(decryptedCountLabel);
                        animateLabel(successRateLabel);
                    });

                } catch (SQLException e) {
                    e.printStackTrace();
                    throw e;
                }
                return null;
            }
        };

        statsTask.setOnFailed(event -> {
            showError("Failed to load statistics");
            event.getSource().getException().printStackTrace();
        });

        new Thread(statsTask, "load-stats-thread").start();
    }


    private String formatOperation(String operation) {
        if (operation == null) return "Unknown";

        switch (operation.toLowerCase()) {
            case "encrypt":
                return "🔒 Encrypt";
            case "decrypt":
                return "🔓 Decrypt";
            default:
                return operation;
        }
    }


    private void animateLabel(Label label) {
        label.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(500), label
        );
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }



    /**
     * Show error alert
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
