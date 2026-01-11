package com.example.cryptofile;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogDAO {
    public LogDAO() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.print(" ");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public ObservableList<LogInfo> getAllLogs() {
        String query = "SELECT al.*, u.username " +
                "FROM activity_logs al " +
                "LEFT JOIN users u ON al.user_id = u.user_id " +
                "ORDER BY al.timestamp DESC";
        ObservableList<LogInfo> logs = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                LogInfo log = new LogInfo();
                log.setLog_id(rs.getInt("log_id"));
                log.setUser_id(rs.getInt("user_id"));
                log.setFile_id(rs.getInt("file_id"));
                log.setAction(rs.getString("action"));
                log.setStatus(rs.getString("status"));
                log.setFile_name(rs.getString("file_name"));
                log.setFile_size(rs.getLong("file_size"));
                log.setUser_name(rs.getString("username"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return logs;
    }

    public static int logActivity(LogInfo logInfo) {
        String query = "INSERT INTO activity_logs " +
                "(user_id, file_id, action, status, file_name, file_size, timestamp)"+
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             var statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, logInfo.getUser_id());
            if(logInfo.getFile_id()!=0){
                statement.setInt(2, logInfo.getFile_id());
            } else {
                statement.setNull(2, Types.INTEGER);
            }
            statement.setString(3, logInfo.getAction());
            statement.setString(4, logInfo.getStatus());
            statement.setString(5, logInfo.getFile_name());
            statement.setLong(6, logInfo.getFile_size());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                try{
                    ResultSet resultSet = statement.getGeneratedKeys();
                    if(resultSet.next()){
                        return  resultSet.getInt(1);
                    }
                } catch (SQLException e){
                    e.printStackTrace();
                }
                System.out.println("A new log record was inserted successfully!");
            } else {
                System.out.println("Failed to insert the log record.");
            }

            return -1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void logSuccess(LogInfo logInfo) {
        try{
            logActivity(logInfo);
        } catch (Exception e) {
            System.out.println("Failed to log success activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void logFailure(LogInfo logInfo) {
        try{
            logActivity(logInfo);
        } catch (Exception e) {
            System.out.println("Failed to log failure activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void logCancelled(LogInfo logInfo) {
        try{
            logActivity(logInfo);
        } catch (Exception e) {
            System.out.println("Failed to log cancelled activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<LogInfo> getAllLogsByUserID(int user_id, int limit) throws Exception{
        List<LogInfo> list = new ArrayList<>();

        String query = "SELECT al.*, f.og_file_name AS file_name, f.og_file_size AS file_size " +
                       "FROM activity_logs al " +
                       "LEFT JOIN files f ON al.file_id = f.file_id " +
                       "WHERE al.user_id = ? " +
                       "ORDER BY al.timestamp DESC " +
                       "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user_id);
            statement.setInt(2, limit);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                LogInfo logInfo = new LogInfo();
                logInfo.setLog_id(resultSet.getInt("log_id"));
                logInfo.setUser_id(resultSet.getInt("user_id"));
                logInfo.setFile_id(resultSet.getInt("file_id"));
                logInfo.setAction(resultSet.getString("action"));
                logInfo.setStatus(resultSet.getString("status"));
                logInfo.setFile_name(resultSet.getString("file_name"));
                logInfo.setFile_size(resultSet.getLong("file_size"));
                logInfo.setTimestamp(resultSet.getTimestamp("timestamp").toLocalDateTime());
                list.add(logInfo);
        }
    }
        return list;
    }

    public static int getTotalLogsCountByUserID(int user_id) throws Exception{
        String query = "SELECT COUNT(*) AS total FROM activity_logs WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user_id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }
        }
    }

    public static int encryptedLogsCountByUserID(int user_id) throws Exception {
        String query = "SELECT COUNT(*) AS total FROM activity_logs WHERE user_id = ? AND action = 'encrypt' AND status = 'success'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user_id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }
        }
    }

    public static int decryptedLogsCountByUserID(int user_id) throws Exception {
        String query = "SELECT COUNT(*) AS total FROM activity_logs WHERE user_id = ? AND action = 'decrypt' AND status = 'success'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user_id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }
        }
    }

    public static double successRateByUserID(int user_id) throws Exception {
        String query = "SELECT " +
                "SUM(CASE WHEN status = 'success' THEN 1 ELSE 0 END) AS success_count, " +
                "COUNT(*) AS total_count " +
                "FROM activity_logs " +
                "WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user_id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int successCount = resultSet.getInt("success_count");
                int totalCount = resultSet.getInt("total_count");
                if (totalCount == 0) {
                    return 0.0;
                }
                return  ((double) successCount / (double) totalCount )* 100;
            } else {
                return 0.0;
            }
        }
    }

    public static List<LogInfo> getTodayLogs(int userId) throws Exception{
        List<LogInfo> list = new ArrayList<>();

        String query = "SELECT * FROM activity_logs WHERE user_id = ? AND DATE(timestamp) = CURDATE() ORDER BY timestamp DESC";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                LogInfo logInfo = new LogInfo();
                logInfo.setLog_id(resultSet.getInt("log_id"));
                logInfo.setUser_id(resultSet.getInt("user_id"));
                logInfo.setFile_id(resultSet.getInt("file_id"));
                logInfo.setAction(resultSet.getString("action"));
                logInfo.setStatus(resultSet.getString("status"));
                logInfo.setFile_name(resultSet.getString("file_name"));
                logInfo.setFile_size(resultSet.getLong("file_size"));
                logInfo.setTimestamp(resultSet.getTimestamp("timestamp").toLocalDateTime());
                list.add(logInfo);
            }
        }
        return list;
    }

    public Map<String, Integer> getSuccessCountByMonth() {
       Map<String, Integer> monthlyData = new java.util.LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String query = "SELECT MONTH(timestamp) as month, COUNT(*) as count " +
                "FROM activity_logs " +
                "WHERE YEAR(timestamp) = YEAR(CURDATE()) AND status = 'success' " +
                "GROUP BY MONTH(timestamp) " +
                "ORDER BY MONTH(timestamp)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int monthNum = rs.getInt("month");
                int count = rs.getInt("count");
                monthlyData.put(months[monthNum - 1], count);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Fill in months with no data
        for (String month : months) {
            monthlyData.putIfAbsent(month, 0);
        }

        return monthlyData;
    }

    public Map<String, Integer> getFailureCountByMonth() {
        Map<String, Integer> monthlyData = new java.util.LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String query = "SELECT MONTH(timestamp) as month, COUNT(*) as count " +
                "FROM activity_logs " +
                "WHERE YEAR(timestamp) = YEAR(CURDATE()) AND status = 'failure' " +
                "GROUP BY MONTH(timestamp) " +
                "ORDER BY MONTH(timestamp)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int monthNum = rs.getInt("month");
                int count = rs.getInt("count");
                monthlyData.put(months[monthNum - 1], count);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Fill in months with no data
        for (String month : months) {
            monthlyData.putIfAbsent(month, 0);
        }

        return monthlyData;
    }

    public int getTotalOperationsCount() {
        String query = "SELECT COUNT(*) as total FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }



}
