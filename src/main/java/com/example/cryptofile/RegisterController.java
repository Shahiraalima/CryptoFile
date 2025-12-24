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
    @FXML private BorderPane registerRoot;
    @FXML private StackPane registerPane;
    @FXML private VBox registerBox;
    @FXML private Label createMsg;

    @FXML private Button backBtn;

    @FXML private VBox userBox;
    @FXML private Label userMsg;
    @FXML private TextField userField;

    @FXML private VBox emailBox;
    @FXML private Label emailMsg;
    @FXML private TextField emailField;

    @FXML private StackPane passwordStack;
    @FXML private PasswordField passwordField;
    @FXML private TextField showPasswordField;
    @FXML private ToggleButton eyeIcon;

    @FXML private StackPane confirmPass;
    @FXML private PasswordField confirmPassField;
    @FXML private TextField confirmShow;
    @FXML private ToggleButton eyeButton;

    @FXML private Button registerButton;

    @FXML private Label validMsg;

    @FXML
    public void switchToLoginScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }



}
