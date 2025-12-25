package com.example.cryptofile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;


public class RegisterController {
    @FXML
    Button backBtn;

    @FXML
    private Label userMsg;
    @FXML
    private TextField userField;

    @FXML
    private Label emailMsg;
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField showPasswordField;
    @FXML
    private ToggleButton eyeIcon;
    @FXML
    private Label requirementsMsg;
    @FXML
    private Label passwordStrengthMsg;

    @FXML
    private PasswordField confirmPassField;
    @FXML
    private TextField confirmShow;
    @FXML
    private ToggleButton eyeButton;

    @FXML
    private Label validMsg;
    @FXML
    private Hyperlink loginLink;


    // Initialize method to set up listeners for real-time validation for username, email, and password fields
    @FXML
    public void initialize() {
        loginLink.setVisible(false);

        userField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                String username = userField.getText();
                if (!username.isEmpty()) {
                    UserDAO userDAO = new UserDAO();
                    boolean exists = userDAO.checkUsernameExists(username);
                    if (exists) {
                        userMsg.setText("Username is already taken.");
                    } else {
                        userMsg.setText("");
                    }
                }
            }
        });

        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                String email = emailField.getText();
                if (!email.isEmpty()) {
                    UserDAO userDAO = new UserDAO();
                    boolean exists = userDAO.checkEmailExists(email);
                    if (exists) {
                        emailMsg.setText("Email is already registered.");
                    } else {
                        emailMsg.setText("");
                    }
                }
            }
        });

        Shared.setupPasswordStrengthListener(passwordField, requirementsMsg, passwordStrengthMsg);

    }


    // Handle registration logic upon clicking register button
    @FXML
    public void HandleRegister(ActionEvent event) throws IOException {
        String username = userField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPassField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            validMsg.setText("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            validMsg.setText("Passwords do not match.");
            return;
        }

        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("user");

        UserDAO userDAO = new UserDAO();
        boolean success = userDAO.registerUser(user);
        if (success) {
            validMsg.setStyle("-fx-text-fill: green");
            validMsg.setText("Registration is successful! You can now log in.");
            loginLink.setVisible(true);
        } else {
            validMsg.setText("Registration failed. Please try again.");
        }
    }

    // Toggle visibility for password field
    @FXML
    public void togglePasswordVisibility(ActionEvent event) throws IOException {
        Shared.setupPassWordVisibilityToggle(passwordField, showPasswordField, eyeIcon);
    }

    // Toggle visibility for confirm password field
    @FXML
    public void toggleConfirmPasswordVisibility(ActionEvent event) throws IOException {
        Shared.setupPassWordVisibilityToggle(confirmPassField, confirmShow, eyeButton);
    }


    // Navigate back to log in scene upon clicking back button and the back to login hyperlink upon registration successfully
    @FXML
    public void switchToLoginScene(ActionEvent event) throws IOException {
        Shared shared = new Shared();
        shared.switchScene(event, "login.fxml");
    }

    // Add CSS to the scene
    public void setScene(Scene scene) {
        scene.getStylesheets().add(getClass().getResource("/styles/register.css").toExternalForm());
    }


}
