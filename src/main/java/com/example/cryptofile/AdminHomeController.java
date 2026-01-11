package com.example.cryptofile;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.util.Map;

public class AdminHomeController {
    @FXML private Label totalUsersLabel;
    @FXML private Label EncryptedFilesLabel;
    @FXML private Label DecryptedFilesLabel;
    @FXML private Label toalOperationsLabel;

    @FXML private BarChart<String, Number> FilesBarChart;
    @FXML private LineChart<String, Number> UsersLineChart;
    @FXML private PieChart FileTypePieChart;
    @FXML private LineChart<String, Number> successFailureLineChart;

    private UserDAO userDAO;
    private FileDAO fileDAO;
    private LogDAO logDAO;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        fileDAO = new FileDAO();
        logDAO = new LogDAO();

        loadStatistics();
        loadCharts();
    }

    private void loadStatistics() {
        try {
            // Load total users
            int totalUsers = userDAO.getTotalUsersCount();
            totalUsersLabel.setText(String.valueOf(totalUsers));

            // Load encrypted files count
            int encryptedFiles = fileDAO.getFilesCountByStatus("encrypted");
            EncryptedFilesLabel.setText(String.valueOf(encryptedFiles));

            // Load decrypted files count
            int decryptedFiles = fileDAO.getFilesCountByStatus("decrypted");
            DecryptedFilesLabel.setText(String.valueOf(decryptedFiles));

            // Load total operations
            int totalOperations = logDAO.getTotalOperationsCount();
            toalOperationsLabel.setText(String.valueOf(totalOperations));

        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCharts() {
        loadFilesBarChart();
        loadUsersLineChart();
        loadFileTypePieChart();
        loadSuccessFailureLineChart();
    }

    private void loadFilesBarChart() {
        try {
            XYChart.Series<String, Number> encryptedSeries = new XYChart.Series<>();
            encryptedSeries.setName("Encrypted");

            XYChart.Series<String, Number> decryptedSeries = new XYChart.Series<>();
            decryptedSeries.setName("Decrypted");

            // Get data from database
            Map<String, Integer> encryptedData = fileDAO.getEncryptedFilesByMonth();
            Map<String, Integer> decryptedData = fileDAO.getDecryptedFilesByMonth();

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            for (String month : months) {
                int encCount = encryptedData.getOrDefault(month, 0);
                int decCount = decryptedData.getOrDefault(month, 0);

                encryptedSeries.getData().add(new XYChart.Data<>(month, encCount));
                decryptedSeries.getData().add(new XYChart.Data<>(month, decCount));
            }

            FilesBarChart.getData().addAll(encryptedSeries, decryptedSeries);
        } catch (Exception e) {
            System.err.println("Error loading files bar chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUsersLineChart() {
        try {
            XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
            userSeries.setName("New Users");

            // Get data from database
            Map<String, Integer> userData = userDAO.getUserRegistrationsByMonth();

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            for (String month : months) {
                int count = userData.getOrDefault(month, 0);
                userSeries.getData().add(new XYChart.Data<>(month, count));
            }

            UsersLineChart.getData().add(userSeries);
        } catch (Exception e) {
            System.err.println("Error loading users line chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFileTypePieChart() {
        try {
            Map<String, Integer> fileTypeData = fileDAO.getFileTypeDistribution();

            FileTypePieChart.getData().clear();

            for (Map.Entry<String, Integer> entry : fileTypeData.entrySet()) {
                if (entry.getValue() > 0) {
                    PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
                    FileTypePieChart.getData().add(slice);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading file type pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSuccessFailureLineChart() {
        try {
            XYChart.Series<String, Number> successSeries = new XYChart.Series<>();
            successSeries.setName("Success");

            XYChart.Series<String, Number> failureSeries = new XYChart.Series<>();
            failureSeries.setName("Failure");

            Map<String, Integer> successData = logDAO.getSuccessCountByMonth();
            Map<String, Integer> failureData = logDAO.getFailureCountByMonth();

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            for (String month : months) {
                int successCount = successData.getOrDefault(month, 0);
                int failureCount = failureData.getOrDefault(month, 0);

                successSeries.getData().add(new XYChart.Data<>(month, successCount));
                failureSeries.getData().add(new XYChart.Data<>(month, failureCount));
            }

            successFailureLineChart.getData().addAll(successSeries, failureSeries);
        } catch (Exception e) {
            System.err.println("Error loading success/failure line chart: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
