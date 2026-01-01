package com.example.cryptofile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;


public class LoginController {
    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane loginPane;
    @FXML
    private VBox loginVBox;

    @FXML
    private Label welcomeText;
    @FXML
    private Label loginLabel;
    @FXML
    private ImageView logo;

    @FXML
    private StackPane passwordPane;
    @FXML
    private TextField username, showPassword;
    @FXML
    private PasswordField password;
    @FXML
    private Button loginButton;
    @FXML
    private ToggleButton eyeButton;
    @FXML
    private Label errorMsg;

    @FXML
    private HBox createAccountHBox;
    @FXML
    private Hyperlink registerLink;
    @FXML
    private Text message;

    @FXML
    private Stage stage;
    @FXML
    private Scene scene;


    // Toggle password visibility
    @FXML
    public void togglePasswordVisibility(ActionEvent event) throws IOException {
        Shared.setupPassWordVisibilityToggle(password, showPassword, eyeButton);
    }


    // Handle login action and verify credentials
    @FXML
    public void HandleLogin(ActionEvent event) throws IOException {
        String user = username.getText();
        String pass = password.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorMsg.setText("Please enter username and password");
        } else {
            UserDAO userDAO = new UserDAO();
            UserInfo validUser = userDAO.loginVerify(user, pass);
            if (validUser != null) {
                if (validUser.getRole().equals("admin")) {
                    switchToAdminHomeScene(event);
                } else {
                    switchToUserHomeScene(event);
                }
            } else {
                errorMsg.setText("Invalid username or password");
            }
        }
    }


    // Switch to registration scene
    @FXML
    public void switchToRegisterScene(ActionEvent event) throws IOException {
        Shared shared = new Shared();
        shared.switchScene(event, "register.fxml");
    }

    // Switch to user home scene
    @FXML
    public void switchToUserHomeScene(ActionEvent event) throws IOException {
        Shared shared = new Shared();
        shared.switchScene(event, "userNavigation.fxml");
    }

    // Switch to admin home scene
    @FXML
    public void switchToAdminHomeScene(ActionEvent event) throws IOException {
        Shared shared = new Shared();
        shared.switchScene(event, "adminNavigation.fxml");
    }

}
