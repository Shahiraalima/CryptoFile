package com.example.cryptofile;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class UserNavigationController {

    @FXML private StackPane contentPane;
    @FXML private Label usernameLabel;

    private String currentUser;

    @FXML
    public void initialize() {
        usernameLabel.setText(SessionManager.loggedInUser.getUsername());
        loadView("userHome.fxml");
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
    public void loadDashboard() {
        loadView("userHome.fxml");
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
    @FXML
    public void loadMyFiles() {
        loadView("userMyFiles.fxml");
    }
    @FXML
    public void loadProfile() {
        loadView("userProfile.fxml");
    }
    @FXML
    public void handleLogout() {
        // Implement logout logic here
    }

}