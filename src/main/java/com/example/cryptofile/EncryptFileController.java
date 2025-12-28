package com.example.cryptofile;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @FXML private Button encryptBtn;
    @FXML private Button resetBtn;
    @FXML private Label statusLabel;

    private List<File> selectedFiles;
    private ObservableList<File> fileList = FXCollections.observableArrayList();


    //TODO: password must be of 8 characters regardless of strong or weak..show alert..
    //TODO: password match bug... popup is opening regardless of match or not..
    // TODO: delete og file... zip file
    //TODO: for popup close button think something like.. are you sure you want to cancel encryption? progress will be lost.


    @FXML
    public void initialize() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        Shared.setupPasswordStrengthListener(passwordField, requirementsLabel, passwordStrengthLabel);

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
    public void handleBrowseFiles() { //TODO: add the drag and drop
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Encrypt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser. ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt", "*.xlsx", "*.pptx"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.svg"),
                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi", "*.mkv", "*.mov", "*.wmv"),
                new FileChooser. ExtensionFilter("Audio", "*.mp3", "*.wav", "*.flac", "*.aac"),
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
            fileList.setAll(listView.getItems());
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

    @FXML
    public void handleEncryptButton() {
        if(listView.getItems().isEmpty()) {
            Label alertLabel = new Label("Please Select a File to Start Encryption");
            Shared.showAlert(alertLabel);
            return;
        } else if (passwordField.getText().isEmpty()) {
            Label alertLabel = new Label("Please Enter a Password to Start Encryption");
            Shared.showAlert(alertLabel);
            return;
        } else if (confirmPasswordField.getText().isEmpty()) {
            Label alertLabel = new Label("Please Confirm Your Password to Start Encryption");
            Shared.showAlert(alertLabel);
            return;
        } else {
            boolean check = checkPasswordMatch();
            System.out.println(check);
            if(!check) return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("encProgressPopup.fxml"));
            Parent popup = loader.load();

            EncryptPopupController controller = loader.getController();
            controller.loadData(fileList, passwordField.getText());

            Scene scene = new Scene(popup, 600, 500);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Cryptofile");

            controller.setStage(stage);

            stage.setOnCloseRequest(event -> {
                event.consume();
                controller.handleCloseButton();
            });

            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    private boolean checkPasswordMatch() {
        AtomicBoolean allMatch = new AtomicBoolean(true);
        confirmPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();
                if(!confirmPassword.isEmpty()) {
                    if(!confirmPassword.equals(password)) {
                        passwordMatchLabel.setStyle("-fx-text-fill: red;");
                        passwordMatchLabel.setText("Passwords do not match");
                        allMatch.set(false);
                    }
                } else {
                    passwordMatchLabel.setText("");
                    allMatch.set(true);
                }
            }
        });
        return allMatch.get();
    }

    // Customize the list view to show file name, size and remove button
    private void customListview() {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label labelName = new Label(item.getName());
                    Label labelSize = new Label("(" + item.length()/1024 + " KB)"); //TODO: create function for file size
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
