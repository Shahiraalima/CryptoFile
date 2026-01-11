package com.example.cryptofile;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserHomeController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox activityLogsBox;


    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome back, " + SessionManager.loggedInUser.getUsername().split(" ")[0] + "!");
        loadTodayActivityLogs();
    }

    private void loadTodayActivityLogs() {
        Task<ObservableList<LogInfo>> task = new Task<ObservableList<LogInfo>>() {
            @Override
            protected ObservableList<LogInfo> call() throws Exception {
                List<LogInfo> logs = LogDAO.getTodayLogs(SessionManager.loggedInUser.getUser_id());
                return  FXCollections.observableArrayList(logs);
            }
        };

        task.setOnSucceeded(event -> {
            ObservableList<LogInfo> data = task.getValue();
            activityLogsBox.getChildren().clear();

            if(data.isEmpty()) {
                Label label = new Label("No activity today.");
                activityLogsBox.getChildren().add(label);
            } else {
                for(int i = 0; i < data.size(); i++) {
                    HBox row = createLogRow(data.get(i));
                    activityLogsBox.getChildren().add(row);

                    if (i < data.size() - 1) {
                        Separator separator = new Separator();
                        separator.setStyle("-fx-padding: 5 0;");
                        activityLogsBox.getChildren().add(separator);
                    }
                }
            }
        });

        task.setOnFailed(event -> {
            Throwable e = task.getException();
            e.printStackTrace();
        });

        new Thread(task).start();
    }


    private  HBox createLogRow(LogInfo log) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label actionLabel = new Label();
        if(log.getAction().equals("Encrypt"))  {
            actionLabel.setGraphic(Shared.createIcon("encryptIcon"));
        } else {
            actionLabel.setGraphic(Shared.createIcon("decryptIcon"));
        }

        VBox vBox = new VBox();
        HBox.setHgrow(vBox, Priority.ALWAYS);


        String actionText = log.getAction().equals("Encrypt") ? "encrypted" : "decrypted";
        String statusText = log.getStatus().equalsIgnoreCase("Success") ? "successfully" : "failed";

        Label fileLabel = new Label(log.getFile_name() + " " + actionText + " " + statusText);
        fileLabel.setStyle("-fx-font-size: 14px;");

        Label timeLabel = new Label(formatTimeAgo(log.getTimestamp()));
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");

        vBox.getChildren().addAll(fileLabel, timeLabel);


        Label label = new Label(log.getStatus());
        label.setStyle("-fx-font-size: 13;");

        HBox hbox = new HBox(label);
        hbox.setAlignment(Pos.CENTER);
        switch (log.getStatus()) {
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

        row.getChildren().addAll(actionLabel, vBox, hbox);
        row.setSpacing(10);
        return row;

    }

    private String formatTimeAgo(LocalDateTime timestamp) {
        Duration duration = Duration.between(timestamp, LocalDateTime.now());

        long hours = duration.toHours();
        long minutes = duration.toMinutes();

        if (hours == 0) {
            if (minutes == 0) {
                return "Just now";
            } else if (minutes == 1) {
                return "1 minute ago";
            } else {
                return minutes + " minutes ago";
            }
        } else if (hours == 1) {
            return "1 hour ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }


    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void loadEncryptFile() {
        loadView("encryptFile.fxml");
    }

    @FXML
    public void loadDecryptFile() {
        loadView("decryptFile.fxml");
    }

    @FXML
    public void loadActivityLogs() {
        loadView("userActivityLogs.fxml");
    }


}