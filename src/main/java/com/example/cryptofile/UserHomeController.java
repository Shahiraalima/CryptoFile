package com.example.cryptofile;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class UserHomeController {
    @FXML private Label welcomeLabel;
    @FXML private StackPane contentPane;


    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome back, " + SessionManager.loggedInUser.getUsername().split(" ")[0] + "!");
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
    public void loadAllFiles() {
        loadView("userMyFiles.fxml");
    }
    @FXML
    public void loadActivityLogs() {
        loadView("userActivityLogs.fxml");
    }


}