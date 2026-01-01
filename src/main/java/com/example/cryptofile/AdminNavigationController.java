package com.example.cryptofile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminNavigationController {
    @FXML
    private StackPane contentPane;
    @FXML
    private Label nameLabel;

    @FXML
    public void initialize() {
        nameLabel.setText(SessionManager.loggedInUser.getUsername());
        loadView("adminHome.fxml");
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
        loadView("adminHome.fxml");
    }

    @FXML
    public void loadUserManagement() {loadView("adminUserManage.fxml");
    }

    @FXML
    public void loadFileManagement() {
        loadView("adminFileManage.fxml");
    }

    @FXML
    public void loadActivityLogs() {
        loadView("adminLogs.fxml");
    }

    @FXML
    public void loadProfile() {
        loadView("adminInfo.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        SessionManager.loggedInUser = null;
        Shared shared = new Shared();
        shared.switchScene(event, "login.fxml");
    }
}
