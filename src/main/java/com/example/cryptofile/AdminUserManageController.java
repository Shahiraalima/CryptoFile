package com.example.cryptofile;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;


public class AdminUserManageController {
    @FXML private Button addUserButton;

    @FXML private Label totalUsersLabel;
    @FXML private Label standardUsersLabel;
    @FXML private Label adminsLabel;
    @FXML private Label newUsersLabel;

    @FXML private TableView<UserInfo> activityTable;
    @FXML private TableColumn<UserInfo, String> usernameColumn;
    @FXML private TableColumn<UserInfo, String> emailColumn;
    @FXML private TableColumn<UserInfo, String> roleColumn;
    @FXML private TableColumn<UserInfo, String> lastActiveColumn;
    @FXML private TableColumn<UserInfo, String> joinColumn;
    @FXML private TableColumn<UserInfo, String> actionColumn;

    private ObservableList<UserInfo> list = FXCollections.observableArrayList();
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        activityTable.getStyleClass().add("table-view");
        activityTable.setFixedCellSize(50);

        setupTableColumns();
        loadStatistics();
        loadUsers();
    }

    private void loadStatistics() {
        try {
            int totalUsers = userDAO.getTotalUsersCount();
            totalUsersLabel.setText(String.valueOf(totalUsers));

            int standardUsers = userDAO.getUserCountByRole("User");
            standardUsersLabel.setText(String.valueOf(standardUsers));

            int admins = userDAO.getUserCountByRole("Admin");
            adminsLabel.setText(String.valueOf(admins));

            int newUsers = userDAO.getNewUsersThisMonth();
            newUsersLabel.setText(String.valueOf(newUsers));

        } catch (Exception e) {
            System.err.println("Error loading user statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private  void setupTableColumns() {
        usernameColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getUsername());
        });
        usernameColumn.setStyle( "-fx-alignment: CENTER;");

        emailColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getEmail());
        });
        emailColumn.setStyle( "-fx-alignment: CENTER;");

        roleColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getRole());
        });
        roleColumn.setStyle( "-fx-alignment: CENTER;");

        lastActiveColumn.setCellValueFactory(cellData -> {
            LocalDateTime lastActive = cellData.getValue().getLast_active();
            if (lastActive != null) {
                return new SimpleStringProperty(lastActive.toString());
            }
            return new SimpleStringProperty("Never");
        });
        lastActiveColumn.setStyle( "-fx-alignment: CENTER;");

        joinColumn.setCellValueFactory(cellData -> {
            String data = String.valueOf(cellData.getValue().getAccount_created());
            return new SimpleStringProperty(data);
        });
        joinColumn.setStyle( "-fx-alignment: CENTER;");

        actionColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty("");
        });
        actionColumn.setStyle( "-fx-alignment: CENTER;");

        actionColumn.setCellFactory(column -> new TableCell<UserInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Button editButton = new Button("Edit");
                    Button deleteButton = new Button("Delete");
                    HBox hbox = new HBox(editButton, deleteButton);
                    hbox.setAlignment(Pos.CENTER);
                    hbox.setSpacing(10);
                    setText(null);
                    setGraphic(hbox);

                    editButton.setOnAction(event -> {
                        UserInfo userInfo = getTableView().getItems().get(getIndex());
                        editUser(userInfo);
                        loadUsers();
                    });

                    deleteButton.setOnAction(event -> {
                        UserInfo userInfo = getTableView().getItems().get(getIndex());
                        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmationAlert.setTitle("Delete User");
                        confirmationAlert.setHeaderText("Are you sure you want to delete user: " + userInfo.getUsername() + "?");
                        confirmationAlert.setContentText("This action cannot be undone.");

                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                userDAO.deleteUser(userInfo.getUsername());
                                loadUsers();
                            }
                        });
                    });


                }
            }
        });

        activityTable.setPlaceholder(new Label("No user records found"));

    }

    private void loadUsers() {
        try {
            list.clear();
            list.addAll(userDAO. getAllUsers());
            activityTable.setItems(list);
            activityTable.prefHeightProperty().bind(Bindings.min(activityTable.fixedCellSizeProperty().multiply(Bindings.size(activityTable.getItems()).add(1.01)), 600.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addUser() {
        Dialog<UserInfo> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter user details:");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Admin", "User");
        roleComboBox.setValue("User");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(usernameField.getText());
                userInfo.setEmail(emailField.getText());
                userInfo.setPassword(passwordField.getText());
                userInfo.setRole(roleComboBox.getValue());
                userInfo.setAccount_created(LocalDateTime.now());
                return userInfo;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(userInfo -> {
            userDAO.registerUser(userInfo);
            loadUsers();
        });
    }

    private void editUser(UserInfo userInfo) {
        Dialog<UserInfo> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Modify user details:");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField(userInfo.getUsername());
        TextField emailField = new TextField(userInfo.getEmail());
        TextField fullNameField = new TextField(userInfo.getFullName());
        CheckBox resetPasswordCheckBox = new CheckBox("Reset Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password");
        passwordField.setDisable(true);
        resetPasswordCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            passwordField.setDisable(!isNowSelected);
        });

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Admin", "User");
        roleComboBox.setValue(userInfo.getRole());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(resetPasswordCheckBox, 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleComboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                userInfo.setUsername(usernameField.getText());
                userInfo.setEmail(emailField.getText());
                userInfo.setFullName(fullNameField.getText());
                if (resetPasswordCheckBox.isSelected()) {
                    userInfo.setPassword(passwordField.getText());
                }
                userInfo.setRole(roleComboBox.getValue());
                return userInfo;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(info -> {
            userDAO.updateUserInfo(info);
            loadUsers();
        });
    }






}
