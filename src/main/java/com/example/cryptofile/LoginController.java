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
import java.sql.Connection;
import java.sql.SQLException;


public class LoginController {
    @FXML private BorderPane rootPane;
    @FXML private StackPane loginPane;
    @FXML private VBox loginVBox;

    @FXML private Label welcomeText;
    @FXML private Label loginLabel;
    @FXML private ImageView logo;

    @FXML private StackPane passwordPane;
    @FXML private TextField username, showPassword;
    @FXML private PasswordField password;
    @FXML private Button loginButton;
    @FXML private ToggleButton eyeButton;
    @FXML private Label errorMsg;

    @FXML private HBox createAccountHBox;
    @FXML private Hyperlink registerLink;
    @FXML private Text message;

    @FXML private Stage stage;
    @FXML private Scene scene;


    @FXML
    public void togglePasswordVisibility(ActionEvent event) throws IOException {
        showPassword.textProperty().bindBidirectional(password.textProperty());
        boolean visible = eyeButton.isSelected();
        password.setVisible(!visible);
        password.setManaged(!visible);
        showPassword.setVisible(visible);
        showPassword.setManaged(visible);
        if (visible) {
            showPassword.requestFocus();
            showPassword.positionCaret(showPassword.getText().length());
            eyeButton.setText("\uD83D\uDC41");
        }
        else {
            password.requestFocus();
            password.positionCaret(password.getText().length());
            eyeButton.setText("\uD83D\uDC41");
        }
    }

    @FXML
    public void HandleLogin(ActionEvent event) throws IOException, SQLException {
        String user = username.getText();
        String pass = password.getText();

        if(user.isEmpty() || pass.isEmpty()) {
            errorMsg.setText("Please enter username and password");
            return;
        } else {
            UserDAO userDAO = new UserDAO();
            UserInfo validUser = userDAO.loginVerify(user, pass);
            if (validUser != null) {
                if(validUser.getRole().equals("admin")) {
                    switchToAdminHomeScene(event);
                }
                else {
                    switchToUserHomeScene(event);
                }
            } else {
                errorMsg.setText("Invalid username or password");
            }

        }
    }



    @FXML
    public void switchToRegisterScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
        Parent root = loader.load();
        stage=(Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToUserHomeScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("userHome.fxml"));
        Parent root = loader.load();
        stage=(Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToAdminHomeScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("adminHome.fxml"));
        Parent root = loader.load();
        stage=(Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setTitle("CryptoFile");
        stage.setScene(scene);
        stage.show();
    }

}
