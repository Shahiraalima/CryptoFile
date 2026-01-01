package com.example.cryptofile;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class AdminFileManageController {
    @FXML private Button addFileButton;

    @FXML private Label totalFileLabel;
    @FXML private Label encryptedLabel;
    @FXML private Label decryptedLabel;

    @FXML private TableView<FileInfo> activityTable;
    @FXML private TableColumn<FileInfo, String> ogFileNameColumn;
    @FXML private TableColumn<FileInfo, String> ownerColumn;
    @FXML private TableColumn<FileInfo, String> statusColumn;
    @FXML private TableColumn<FileInfo, String> newFileNameColumn;
    @FXML private TableColumn<FileInfo, String> newFileSizeColumn;
    @FXML private TableColumn<FileInfo, String> dateColumn;
    @FXML private TableColumn<FileInfo, String> actionColumn;

    private ObservableList<FileInfo> list = FXCollections.observableArrayList();
    private FileDAO fileDAO = new FileDAO();

    @FXML
    public void initialize() {
        activityTable.getStyleClass().add("table-view");
        activityTable.setFixedCellSize(50);

        setupTableColumns();
        loadfiles();
    }

    private  void setupTableColumns() {
        ogFileNameColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getOg_file_name());
        });
        ogFileNameColumn.setStyle( "-fx-alignment: CENTER;");

        statusColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getStatus());
        });
        statusColumn.setStyle( "-fx-alignment: CENTER;");

        newFileNameColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getEncrypted_file_name());
        });
        newFileNameColumn.setStyle( "-fx-alignment: CENTER;");

        newFileSizeColumn.setCellValueFactory(cellData -> {
            long sizeInBytes = cellData.getValue().getEncrypted_file_size();
            String sizeString = Shared.formatFileSize(sizeInBytes);
            return new SimpleStringProperty("");
        });
        newFileSizeColumn.setStyle( "-fx-alignment: CENTER;");

        dateColumn.setCellValueFactory(cellData -> {
            String data = String.valueOf(cellData.getValue().getEncrypted_at());
            return new SimpleStringProperty(data);
        });
        dateColumn.setStyle( "-fx-alignment: CENTER;");

        actionColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty("");
        });
        actionColumn.setStyle( "-fx-alignment: CENTER;");

        actionColumn.setCellFactory(column -> new TableCell<FileInfo, String>() {
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
                        FileInfo fileInfo = getTableView().getItems().get(getIndex());
//                        editUser(userInfo);
                        loadfiles();
                    });

                    deleteButton.setOnAction(event -> {
                        FileInfo fileInfo = getTableView().getItems().get(getIndex());
                        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmationAlert.setTitle("Delete User");
                        confirmationAlert.setHeaderText("Are you sure you want to delete user: " + fileInfo.getOg_file_name() + "?");
                        confirmationAlert.setContentText("This action cannot be undone.");

                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
//                                fileDAO.deleteUser(userInfo.getUsername());
                                loadfiles();
                            }
                        });
                    });


                }
            }
        });

        activityTable.setPlaceholder(new Label("No file records found"));

    }

    private void loadfiles() {
        try {
            list.clear();
//            list.addAll(fileDAO.getAllFiles());
            activityTable.setItems(list);
            activityTable.prefHeightProperty().bind(Bindings.min(activityTable.fixedCellSizeProperty().multiply(Bindings.size(activityTable.getItems()).add(1.01)), 600.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
