package com.example.cryptofile;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserActivityLogsController {

    @FXML private Label totalOperationsLabel;
    @FXML private Label encryptedCountLabel;
    @FXML private Label decryptedCountLabel;
    @FXML private Label successRateLabel;

    @FXML private Label totalOperationsIcon;
    @FXML private Label encryptedCountIcon;
    @FXML private Label decryptedCountIcon;
    @FXML private Label successRateIcon;

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
        activityTable.getStyleClass().add("table-view");
        activityTable.setFixedCellSize(50);

        setupTableColumns();
        loadActivityLogs();
        loadStatistics();
    }

    private void setupTableColumns() {

        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            return new SimpleStringProperty(status);
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
                    Label label = new Label();
                    switch (item) {
                        case "Success":
                            label.setGraphic(Shared.createIcon("successIcon"));
                            break;
                        case "Failed":
                            label.setGraphic(Shared.createIcon("failedIcon"));
                            break;
                        case "Cancelled":
                            label.setGraphic(Shared.createIcon("cancelledIcon"));
                            break;
                        default:
                            label.setGraphic(null);
                            break;
                    }
                    setText(null);
                    setGraphic(label);
                }
            }
        });


        fileNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFile_name())
        );
        fileNameColumn.setStyle("-fx-alignment: CENTER;");
        fileNameColumn.setCellFactory(column -> new TableCell<LogInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-font-size: 16;");
                    setText(null);
                    setGraphic(label);
                }
            }
        });


        operationColumn.setCellValueFactory(cellData -> {
            String operation = cellData.getValue().getAction();
            return new SimpleStringProperty(operation);
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
                    Node icon = null;
                    switch (item) {
                        case "Encrypt":
                            icon= Shared.createIcon("encryptIcon");
                            break;
                        case "Decrypt":
                            icon= Shared.createIcon("decryptIcon");
                            break;
                        default:
                            icon= null;
                            break;
                    }
                    Label label = new Label(item);
                    label.setStyle("-fx-font-size: 16;");
                    HBox hbox = new HBox(icon, label);
                    hbox.alignmentProperty().setValue(Pos.CENTER);
                    hbox.setStyle("-fx-spacing: 3px; -fx-background-color: #0000ff15; -fx-background-radius: 20; -fx-max-height: 17px; -fx-max-width: 80px;");

                    setText(null);
                    setGraphic(hbox);
                }
            }
        });

        sizeColumn.setCellValueFactory(cellData -> {
                    long bytes = cellData.getValue().getFile_size();
                    String formattedSize = Shared.formatFileSize(bytes);
                    return new SimpleStringProperty(formattedSize);
                });
        sizeColumn.setStyle("-fx-alignment: CENTER;");
        sizeColumn.setCellFactory(column -> new TableCell<LogInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-font-size: 16;");
                    setText(null);
                    setGraphic(label);
                }
            }
        });


        dateTimeColumn. setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            String formatted = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm: ss"));
            return new SimpleStringProperty(formatted);
        });
        dateTimeColumn.setStyle("-fx-alignment: CENTER;");
        dateTimeColumn.setCellFactory(column -> new TableCell<LogInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-font-size: 16;");
                    setText(null);
                    setGraphic(label);
                }
            }
        });


        resultColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus())
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
                    Label label = new Label(item);
                    label.setStyle("-fx-font-size: 16;");
                    HBox hbox = new HBox(label);
                    hbox.setAlignment(Pos.CENTER);
                    switch (item) {
                        case "Success":
                            hbox.setStyle("-fx-background-color: #00800014; -fx-background-radius: 20; -fx-max-height: 17px; -fx-max-width: 85px;");
                            break;
                        case "Failed":
                            hbox.setStyle("-fx-background-color: #ffa50014; -fx-background-radius: 20; -fx-max-height: 17px; -fx-max-width: 85px;");
                            break;
                        case "Cancelled":
                            hbox.setStyle("-fx-background-color: #ff000011; -fx-background-radius: 20; -fx-max-height: 17px; -fx-max-width: 85px;");
                            break;
                        default:
                            label.setGraphic(null);
                            break;
                    }
                    setText(null);
                    setGraphic(hbox);
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
            activityTable.prefHeightProperty().bind(Bindings.min(activityTable.fixedCellSizeProperty().multiply(Bindings.size(activityTable.getItems()).add(1.01)), 600.0));
            System.out.println("✓ Loaded " + logs.size() + " activity logs");
        });

        loadTask.setOnFailed(event -> {
            event.getSource().getException().printStackTrace();
        });

        new Thread(loadTask, "load-logs-thread").start();
    }


    private void loadStatistics() {
        Task<Void> statsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    int totalOperations = LogDAO.getTotalLogsCountByUserID(SessionManager.loggedInUser.getUser_id());

                    int encryptedCount = LogDAO.encryptedLogsCountByUserID(SessionManager.loggedInUser.getUser_id());

                    int decryptedCount = LogDAO.decryptedLogsCountByUserID(SessionManager.loggedInUser.getUser_id());

                    double successRate = LogDAO.successRateByUserID(SessionManager.loggedInUser.getUser_id());

                    Platform. runLater(() -> {
                        totalOperationsLabel.setText(String.valueOf(totalOperations));
                        totalOperationsIcon.setGraphic(Shared.createIcon("operationsIcon"));

                        encryptedCountLabel.setText(String.valueOf(encryptedCount));
                        encryptedCountIcon.setGraphic(Shared.createIcon("encryptedIcon"));

                        decryptedCountLabel.setText(String.valueOf(decryptedCount));
                        decryptedCountIcon.setGraphic(Shared.createIcon("decryptedIcon"));

                        successRateLabel.setText(String.format("%.1f%%", successRate));
                        successRateIcon.setGraphic(Shared.createIcon("successRateIcon"));

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
}
