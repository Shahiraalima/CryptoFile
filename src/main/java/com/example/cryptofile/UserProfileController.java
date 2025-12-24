package com.example.cryptofile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class UserProfileController {
    @FXML private Button saveInfoBtn;
    @FXML private Button cancelInfoBtn;
    @FXML private Button changePasswordBtn;
    @FXML private Button cancelPasswordBtn;

    @FXML private Label infoMessageLabel;
    @FXML private Label passwordMessageLabel;


    public void handleSaveInfo(ActionEvent event) throws IOException {
    }

    public void handleCancelInfo(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("userHome.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene=new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }

    public void handleChangePassword(ActionEvent event) throws IOException {
    }

    public void handleCancelPassword(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("userHome.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene=new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }
}
