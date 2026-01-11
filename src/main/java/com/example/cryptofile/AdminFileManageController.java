package com.example.cryptofile;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    private ObservableList<FileInfo> list = FXCollections.observableArrayList();
    private FileDAO fileDAO = new FileDAO();

    @FXML
    public void initialize() {
        activityTable.getStyleClass().add("table-view");
        activityTable.setFixedCellSize(50);

        setupTableColumns();
        loadStats();
        loadfiles();
    }

    private  void setupTableColumns() {
        ogFileNameColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getOg_file_name());
        });
        ogFileNameColumn.setStyle( "-fx-alignment: CENTER;");

        ownerColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getUserName());
        });

        statusColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getStatus());
        });
        statusColumn.setStyle( "-fx-alignment: CENTER;");

        newFileNameColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getNew_file_name());
        });
        newFileNameColumn.setStyle( "-fx-alignment: CENTER;");

        newFileSizeColumn.setCellValueFactory(cellData -> {
            long sizeInBytes = cellData.getValue().getNew_file_size();
            String sizeString = Shared.formatFileSize(sizeInBytes);
            return new SimpleStringProperty(sizeString);
        });
        newFileSizeColumn.setStyle( "-fx-alignment: CENTER;");

        dateColumn.setCellValueFactory(cellData -> {
            String date = Shared.formatDateTime(cellData.getValue().getModified_at());
            return new SimpleStringProperty(date);
        });
        dateColumn.setStyle( "-fx-alignment: CENTER;");

        activityTable.setPlaceholder(new Label("No file records found"));

    }

    private void loadfiles() {
        try {
            list.clear();
            list.addAll(fileDAO.getAllFiles());
            activityTable.setItems(list);
            activityTable.prefHeightProperty().bind(Bindings.min(activityTable.fixedCellSizeProperty().multiply(Bindings.size(activityTable.getItems()).add(1.01)), 600.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStats() {
        try {
            int totalFiles = fileDAO.getTotalFilesCount();
            int encryptedFiles = fileDAO.getFilesCountByStatus("encrypted");
            int decryptedFiles = fileDAO.getFilesCountByStatus("decrypted");

            totalFileLabel.setText(String.valueOf(totalFiles));
            encryptedLabel.setText(String.valueOf(encryptedFiles));
            decryptedLabel.setText(String.valueOf(decryptedFiles));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
