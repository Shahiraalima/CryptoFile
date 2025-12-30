package com.example.cryptofile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {
    public LogDAO() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.print("Connected to database successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static int logActivity(LogInfo logInfo) {
        String query = "INSERT INTO activity_logs " +
                "(user_id, file_id, action, status, file_name, file_size, timestamp)"+
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             var statement = conn.prepareStatement(query)) {

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

    public static void logPending(LogInfo logInfo) {
        try{
            logActivity(logInfo);
        } catch (Exception e) {
            System.out.println("Failed to log pending activity: " + e.getMessage());
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



}
