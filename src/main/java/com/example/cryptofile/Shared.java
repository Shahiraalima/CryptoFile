package com.example.cryptofile;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class Shared {

    public void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }

    public static void showAlert (Label label) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Warning!");
        alert.setHeaderText(label.getText());
        alert.show();
    }

    public static FontIcon createIcon(String styleClass) {
        FontIcon icon = new FontIcon();
        icon.getStyleClass().add(styleClass);
        return icon;
    }

    public static void setupPasswordStrengthListener(PasswordField passwordField, Label requirementsMsg, Label passwordStrengthMsg) {
        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                String password = passwordField.getText();
                if (!password.isEmpty()) {
                    UserDAO userDAO = new UserDAO();
                    String strengthMsg = userDAO.checkPasswordStrength(password);
                    if (!strengthMsg.isEmpty()) {
                        if (!strengthMsg.equals("Strong")) {
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
        } else {
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            eyeIcon.setText("\uD83D\uDC41");
        }
    }

    public static String formatFIleSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        }

        int exp = (int) (Math.log(sizeInBytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", sizeInBytes / Math.pow(1024, exp), pre);
    }
}
