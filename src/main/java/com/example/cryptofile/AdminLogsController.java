package com.example.cryptofile;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminLogsController {

    @FXML private TableView<LogInfo> activityTable;
    @FXML private TableColumn<LogInfo, String> activityColumn;
    @FXML private TableColumn<LogInfo, String> fileNameColumn;
    @FXML private TableColumn<LogInfo, String> userColumn;
    @FXML private TableColumn<LogInfo, String> statusColumn;
    @FXML private TableColumn<LogInfo, String> sizeColumn;
    @FXML private TableColumn<LogInfo, String> dateColumn;

    private ObservableList<LogInfo> list = FXCollections.observableArrayList();
    private LogDAO logDAO = new LogDAO();

    @FXML
    public void initialize() {
        activityTable.getStyleClass().add("table-view");
        activityTable.setFixedCellSize(50);

        setupTableColumns();
        loadLogs();
    }

    private  void setupTableColumns() {
        activityColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getAction());
        });
        activityColumn.setStyle( "-fx-alignment: CENTER;");

        fileNameColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getFile_name());
        });
        fileNameColumn.setStyle( "-fx-alignment: CENTER;");

        userColumn.setCellValueFactory(cellData -> {
            String username = cellData.getValue().getUser_name();
            return new SimpleStringProperty(username != null ? username : "N/A");
        });
        userColumn.setStyle( "-fx-alignment: CENTER;");

        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status : "N/A");
        });
        statusColumn.setStyle( "-fx-alignment: CENTER;");

        sizeColumn.setCellValueFactory(cellData -> {
            Long sizeInBytes = cellData.getValue().getFile_size();
            if (sizeInBytes != null && sizeInBytes > 0) {
                String sizeString = Shared.formatFileSize(sizeInBytes);
                return new SimpleStringProperty(sizeString);
            }
            return new SimpleStringProperty("N/A");
        });
        sizeColumn.setStyle( "-fx-alignment: CENTER;");

        dateColumn.setCellValueFactory(cellData -> {
            String data = String.valueOf(cellData.getValue().getTimestamp());
            return new SimpleStringProperty(data);
        });
        dateColumn.setStyle( "-fx-alignment: CENTER;");

        activityTable.setPlaceholder(new Label("No log records found"));

    }

    private void loadLogs() {
        try {
            list.clear();
            list.addAll(logDAO.getAllLogs());
            activityTable.setItems(list);
            activityTable.prefHeightProperty().bind(Bindings.min(activityTable.fixedCellSizeProperty().multiply(Bindings.size(activityTable.getItems()).add(1.01)), 600.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


