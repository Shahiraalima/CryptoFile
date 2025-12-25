package com.example.cryptofile;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.io.IOException;

public class Shared {

    public static void setupPasswordStrengthListener(PasswordField passwordField, Label requirementsMsg, Label passwordStrengthMsg) {
        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue){
                String password = passwordField.getText();
                if(!password.isEmpty()){
                    UserDAO userDAO = new UserDAO();
                    String strengthMsg = userDAO.checkPasswordStrength(password);
                    if(!strengthMsg.isEmpty()){
                        if(!strengthMsg.equals("Strong")){
                            requirementsMsg.setStyle("-fx-text-fill: red;");
                            requirementsMsg.setText(strengthMsg);
                            passwordStrengthMsg.setStyle("-fx-text-fill: red;");
                            passwordStrengthMsg.setText("Password Strength: Weak");
                        } else {
                            requirementsMsg.setText("");
                            passwordStrengthMsg.setStyle("-fx-text-fill: green;");
                            passwordStrengthMsg.setText("Password Strength: Strong");
                        }
                    } else {
                        requirementsMsg.setText("");
                        passwordStrengthMsg.setText("");
                    }
                }
            }
        });
    }


    public static void setupPassWordVisibilityToggle(PasswordField passwordField, TextField showPasswordField, ToggleButton eyeIcon) {
        showPasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        boolean visible = eyeIcon.isSelected();
        passwordField.setVisible(!visible);
        passwordField.setManaged(!visible);
        showPasswordField.setVisible(visible);
        showPasswordField.setManaged(visible);
        if (visible) {
            showPasswordField.requestFocus();
            showPasswordField.positionCaret(showPasswordField.getText().length());
            eyeIcon.setText("\uD83D\uDC41");
        }
        else {
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            eyeIcon.setText("\uD83D\uDC41");
        }
    }
}
