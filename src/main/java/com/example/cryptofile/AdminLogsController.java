package com.example.cryptofile;

import com.mysql.cj.log.Log;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

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
//        activityColumn.setCellValueFactory(cellData -> {
//            return new SimpleStringProperty(cellData.getValue().getAction());
//        });
//        activityColumn.setStyle( "-fx-alignment: CENTER;");

        fileNameColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getFile_name());
        });
        fileNameColumn.setStyle( "-fx-alignment: CENTER;");

//        userColumn.setCellValueFactory(cellData -> {
//            return new SimpleStringProperty(cellData.getValue().getUser_id());
//        });
//        userColumn.setStyle( "-fx-alignment: CENTER;");

        sizeColumn.setCellValueFactory(cellData -> {
            long sizeInBytes = cellData.getValue().getFile_size();
            String sizeString = Shared.formatFileSize(sizeInBytes);
            return new SimpleStringProperty("");
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
//            list.addAll(fileDAO.getAllFiles());
            activityTable.setItems(list);
            activityTable.prefHeightProperty().bind(Bindings.min(activityTable.fixedCellSizeProperty().multiply(Bindings.size(activityTable.getItems()).add(1.01)), 600.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


