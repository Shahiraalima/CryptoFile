package com.example.cryptofile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class UserProfileController {
    @FXML
    private Button saveInfoBtn;
    @FXML
    private Button cancelInfoBtn;
    @FXML
    private Button changePasswordBtn;
    @FXML
    private Button cancelPasswordBtn;

    @FXML
    private Label infoMessageLabel;
    @FXML
    private Label passwordMessageLabel;

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private Label accountCreatedLabel;

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmNewPasswordField;
    @FXML
    private Label requirementsLabel;
    @FXML
    private Label passWordStrengthLabel;


    @FXML
    public void initialize() {
        fullNameField.setText(SessionManager.loggedInUser.getFullName());
        emailField.setText(SessionManager.loggedInUser.getEmail());
        usernameField.setText(SessionManager.loggedInUser.getUsername());
        accountCreatedLabel.setText(SessionManager.loggedInUser.dateFormatter());

        Shared.setupPasswordStrengthListener(newPasswordField, requirementsLabel, passWordStrengthLabel);
    }


    public void handleSaveInfo(ActionEvent event) throws IOException {
        String newFullName = fullNameField.getText();
        String newEmail = emailField.getText();

        UserDAO userDAO = new UserDAO();

        boolean updateSuccess = userDAO.updateUserInfo(SessionManager.loggedInUser.getUsername(), newFullName, newEmail);

        if (updateSuccess) {
            infoMessageLabel.setText("Information updated successfully.");
            SessionManager.loggedInUser.setFullName(newFullName);
            SessionManager.loggedInUser.setEmail(newEmail);
        } else {
            infoMessageLabel.setStyle("-fx-text-fill: red");
            infoMessageLabel.setText("Failed to update information. Please try again.");
        }
    }

    public void handleCancelInfo(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("userNavigation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }

    public void handleChangePassword(ActionEvent event) throws IOException {
        String currentPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmNewPass = confirmNewPasswordField.getText();

        if (!SessionManager.loggedInUser.getPassword().equals(currentPass)) {
            passwordMessageLabel.setStyle("-fx-text-fill: red;");
            passwordMessageLabel.setText("Current password is incorrect.");
            return;
        }
        if (!newPass.equals(confirmNewPass)) {
            passwordMessageLabel.setStyle("-fx-text-fill: red;");
            passwordMessageLabel.setText("New passwords do not match.");
            return;
        }

        UserDAO userDAO = new UserDAO();
        boolean updateSuccess = userDAO.updateUserPassword(SessionManager.loggedInUser.getUsername(), newPass);
        if (updateSuccess) {
            passwordMessageLabel.setStyle("-fx-text-fill: green;");
            passwordMessageLabel.setText("Password changed successfully.");
            SessionManager.loggedInUser.setPassword(newPass);
        } else {
            passwordMessageLabel.setStyle("-fx-text-fill: red;");
            passwordMessageLabel.setText("Failed to change password. Please try again.");
        }
    }

    public void handleCancelPassword(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("userNavigation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }
}
