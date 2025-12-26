package com.example.cryptofile;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class EncryptFileController {

    @FXML private StackPane browseBox;
    @FXML private Button removeAllBtn;
    @FXML private ListView<File> listView;
    @FXML private Label fileCountLabel;

    @FXML private Label requirementsLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordStrengthLabel;

    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordMatchLabel;

    @FXML private TextField outputFilePath;

    private List<File> selectedFiles;


    @FXML
    public void initialize() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        Shared.setupPasswordStrengthListener(passwordField, requirementsLabel, passwordStrengthLabel);
        checkPasswordMatch(password, confirmPassword);

        customListview();

    }

    // Remove all files from the list view
    @FXML
    public void removeAllFiles() {
        listView.getItems().clear();
        updateListview();
        updateOutputPath();
        listView.setPrefHeight(0);
        fileCountLabel.setText("Selected files (0)");
        removeAllBtn.setVisible(false);
    }

    // Browse and select files to encrypt and add the files to the list view
    @FXML
    public void handleBrowseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Encrypt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser. ExtensionFilter("Documents", "*.pdf", "*. doc", "*.docx", "*.txt", "*.xlsx", "*.pptx"),
                new FileChooser.ExtensionFilter("Images", "*. jpg", "*.jpeg", "*. png", "*.gif", "*.bmp", "*.svg"),
                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*. avi", "*.mkv", "*.mov", "*.wmv"),
                new FileChooser. ExtensionFilter("Audio", "*.mp3", "*. wav", "*.flac", "*.aac"),
                new FileChooser. ExtensionFilter("Archives", "*.zip", "*.rar", "*.7z", "*.tar", "*.gz")
        );

        selectedFiles = fileChooser.showOpenMultipleDialog(browseBox.getScene().getWindow());
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                listView.getItems().add(file);
            }
            if(listView.getItems().size() > 1) {
                removeAllBtn.setVisible(true);
            }
            updateListview();
            updateOutputPath();

            fileCountLabel.setText("Selected files (" + listView.getItems().size() + ")");
        }
    }

    // Browse and select output directory for encrypted files
    @FXML
    public void handleBrowseOutputPath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        if(!listView.getItems().isEmpty()) {
            String parentDir = listView.getItems().getFirst().getParent();
            boolean allSameDir = listView.getItems().stream().allMatch(f -> f.getParent().equals(parentDir));
            if(allSameDir) {
                directoryChooser.setInitialDirectory(new File(parentDir));
            }
        }

        File selectedDirectory = directoryChooser.showDialog(browseBox.getScene().getWindow());
        if (selectedDirectory != null) {
            outputFilePath.setText(selectedDirectory.getAbsolutePath());
        }
    }


    // Reset all fields and clear the list view
    @FXML
    public void handleResetButton() {
        listView.getItems().clear();
        listView.setPrefHeight(0);
        outputFilePath.setText("");
        fileCountLabel.setText("Selected files (0)");
        removeAllBtn.setVisible(false);
        passwordField.clear();
        confirmPasswordField.clear();
        requirementsLabel.setText("");
        passwordStrengthLabel.setText("");
        passwordMatchLabel.setText("");
    }


    // Update the list view height based on the number of items
    private void updateListview() {
        int size = listView.getItems().size();
        int visibleRows = Math.min(size, 3);
        listView.setPrefHeight(visibleRows * 50 + 2);
        listView.setStyle("-fx-padding: 5px;");
    }


    // Update output path if all files are from the same directory
    private void updateOutputPath() {
        if(outputFilePath!=null && !listView.getItems().isEmpty()) {
            String parentDir = listView.getItems().getFirst().getParent();
            boolean allSameDir = listView.getItems().stream().allMatch(f -> f.getParent().equals(parentDir));
            if(allSameDir) {
                outputFilePath.setText(parentDir);
            } else {
                outputFilePath.setText("");
            }
        }
    }

    // Check if password and confirm password match
    private void checkPasswordMatch(String password, String confirmPassword) {
        confirmPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                if(!confirmPassword.isEmpty()) {
                    if(!confirmPassword.equals(password)) {
                        passwordMatchLabel.setStyle("-fx-text-fill: red;");
                        passwordMatchLabel.setText("Passwords do not match");
                    }
                } else {
                    passwordMatchLabel.setText("");
                }
            }
        });
    }

    // Customize the list view to show file name, size and remove button
    private void customListview() {
        listView.setCellFactory(param -> new ListCell<>() {;
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label labelName = new Label(item.getName());
                    Label labelSize = new Label("(" + item.length()/1024 + " KB)");
                    VBox fileInfoBox = new VBox(labelName, labelSize);
                    fileInfoBox.setAlignment(Pos.CENTER_LEFT);

                    Button rmvbutton = new Button("Remove");

                    HBox row = new HBox();
                    row.getChildren().addAll(fileInfoBox, rmvbutton);
                    row.setSpacing(10);
                    HBox.setHgrow(fileInfoBox, Priority.ALWAYS);
                    row.setStyle("-fx-padding: 5px;");
                    setGraphic(row);

                    rmvbutton.setOnAction(event -> {
                        listView.getItems().remove(item);
                        updateListview();
                        if(listView.getItems().isEmpty()) {
                            listView.setPrefHeight(0);
                        }
                        updateOutputPath();
                        fileCountLabel.setText("Selected files (" + listView.getItems().size() + ")");
                    });
                }
            }
        });
    }





}
