package com.example.cryptofile;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class AdminHomeController {
    @FXML private Label totalUsersLabel;
    @FXML private Label EncryptedFilesLabel;
    @FXML private Label DecryptedFilesLabel;
    @FXML private Label toalOperationsLabel;

    @FXML private BarChart<String, Number> FilesBarChart;
    @FXML private LineChart<String, Number> UsersLineChart;
    @FXML private PieChart FileTypePieChart;
    @FXML private LineChart<String, Number> successFailureLineChart;

    @FXML
    public void initialize() {
        loadStatistics();
        loadCharts();
    }

    private void loadStatistics() {
    }

    private void loadCharts() {
        loadFilesBarChart();
        loadUsersLineChart();
        loadFileTypePieChart();
        loadSuccessFailureLineChart();
    }

    private void loadFilesBarChart() {
        XYChart.Series<String, Number> encryptedSeries = new XYChart.Series<>();
        encryptedSeries.setName("Encrypted");

        XYChart.Series<String, Number> decryptedSeries = new XYChart.Series<>();
        decryptedSeries.setName("Decrypted");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int[] encryptedData = {120, 150, 180, 200, 220, 250, 270, 300, 320, 350, 400, 450};

        int[] decryptedData = {100, 130, 160, 180, 200, 230, 250, 280, 300, 330, 380, 420};

        for (int i = 0; i < months.length; i++) {
            encryptedSeries.getData().add(new XYChart.Data<>(months[i], encryptedData[i]));
            decryptedSeries.getData().add(new XYChart.Data<>(months[i], decryptedData[i]));
        }

        FilesBarChart.getData().addAll(encryptedSeries, decryptedSeries);
    }

    private void loadUsersLineChart() {
        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("Active Users");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int[] userData = {50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160};

        for (int i = 0; i < months.length; i++) {
            userSeries.getData().add(new XYChart.Data<>(months[i], userData[i]));
        }

        UsersLineChart.getData().add(userSeries);
    }

    private void loadFileTypePieChart() {
        PieChart.Data documentsSlice = new PieChart.Data("Documents", 30);
        PieChart.Data imageSlice = new PieChart.Data("Image", 25);
        PieChart.Data videosSlice = new PieChart.Data("Videos", 20);
        PieChart.Data otherSlice = new PieChart.Data("Other", 10);

        FileTypePieChart.getData().addAll(documentsSlice, imageSlice, videosSlice, otherSlice);
    }

    private void loadSuccessFailureLineChart() {
        XYChart.Series<String, Number> successSeries = new XYChart.Series<>();
        successSeries.setName("Success");

        XYChart.Series<String, Number> failureSeries = new XYChart.Series<>();
        failureSeries.setName("Failure");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int[] successData = {110, 140, 170, 190, 210, 240, 260, 290, 310, 340, 390, 430};

        int[] failureData = {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 20};

        for (int i = 0; i < months.length; i++) {
            successSeries.getData().add(new XYChart.Data<>(months[i], successData[i]));
            failureSeries.getData().add(new XYChart.Data<>(months[i], failureData[i]));
        }
        successFailureLineChart.getData().addAll(successSeries, failureSeries);
    }



}
