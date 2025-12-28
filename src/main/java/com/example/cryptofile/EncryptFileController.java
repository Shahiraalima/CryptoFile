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
    @FXML private Button browseOutputBtn;

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

        EncryptAndDecryptUtil.customListview(listView, fileCountLabel, outputFilePath);

        browseBox.setOnMouseClicked(event ->
                EncryptAndDecryptUtil.handleBrowseFiles(selectedFiles, listView, fileList, browseBox, removeAllBtn, fileCountLabel, outputFilePath));

        browseOutputBtn.setOnAction(event -> EncryptAndDecryptUtil.handleBrowseOutputPath(listView, browseBox, removeAllBtn, outputFilePath));

        removeAllBtn.setOnAction(event -> EncryptAndDecryptUtil.removeAllFiles(listView, fileCountLabel, removeAllBtn, outputFilePath));

        outputFilePath.setOnAction(event -> EncryptAndDecryptUtil.handleBrowseOutputPath(listView, browseBox, removeAllBtn, outputFilePath));
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
}
