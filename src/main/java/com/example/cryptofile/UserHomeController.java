package com.example.cryptofile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene. control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class UserHomeController {
    @FXML private Label usernameLabel;
    @FXML private Label welcomeLabel;

    private String currentUsername = "Shahira alima";

    @FXML
    public void initialize() {
        usernameLabel.setText(currentUsername);
        welcomeLabel.setText("Welcome back, " + currentUsername.split(" ")[0] + "!");
    }

    @FXML
    private void showDashboard() {
    }

    @FXML
    private void showEncryptFile() {
    }

    @FXML
    private void showDecryptFile() {
    }

    @FXML
    private void showActivityLogs() {
    }

    @FXML
    private void showMyFiles () {
    }

    @FXML
    private void showProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("userProfile.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene=new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleLogout() {
    }
}