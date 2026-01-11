package com.example.cryptofile;

import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


public class FileDAO {

    public FileDAO() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.print(" ");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public ObservableList<FileInfo> getAllFiles() {
        String query = "SELECT f.*, u.username FROM files f " +
                "INNER JOIN users u ON f.user_id = u.user_id";
        ObservableList<FileInfo> files = javafx.collections.FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                FileInfo file = new FileInfo();
                file.setFile_id(rs.getInt("file_id"));
                file.setUser_id(rs.getInt("user_id"));
                file.setOg_file_name(rs.getString("og_file_name"));
                file.setOg_file_size(rs.getLong("og_file_size"));
                file.setOg_file_type(rs.getString("og_file_type"));
                file.setOg_file_hash(rs.getString("og_file_hash"));
                file.setNew_file_name(rs.getString("new_file_name"));
                file.setNew_file_size(rs.getLong("new_file_size"));
                file.setEncrypted_file_hash(rs.getString("encrypted_file_hash"));
                file.setStatus(rs.getString("status"));
                file.setEncrypted_at(rs.getTimestamp("encrypted_at").toLocalDateTime());
                if(rs.getTimestamp("decrypted_at") != null) {
                    file.setDecrypted_at(rs.getTimestamp("decrypted_at").toLocalDateTime());
                }
                if(rs.getString("status").equals("encrypted")) {
                    file.setModified_at(file.getEncrypted_at());
                } else if(file.getStatus().equals("decrypted")) {
                    file.setModified_at(file.getDecrypted_at());
                }
                file.setUserName(rs.getString("username"));
                files.add(file);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return files;
    }


    public void insertFile(FileInfo fileInfo) {
        String query = "INSERT INTO files " +
                "(user_id, og_file_name, og_file_size, og_file_type, og_file_hash, " +
                "new_file_name, new_file_size, encrypted_file_hash, encrypted_at, decrypted_at)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NULL)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, fileInfo.getUser_id());
            statement.setString(2, fileInfo.getOg_file_name());
            statement.setLong(3, fileInfo.getOg_file_size());
            statement.setString(4, fileInfo.getOg_file_type());
            statement.setString(5, fileInfo.getOg_file_hash());
            statement.setString(6, fileInfo.getNew_file_name());
            statement.setLong(7, fileInfo.getNew_file_size());
            statement.setString(8, fileInfo.getEncrypted_file_hash());


            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new encrypted file record was inserted successfully!");
            } else {
                System.out.println("Failed to insert the encrypted file record.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateForReencryption(FileInfo fileInfo) {
        String query = "UPDATE files SET new_file_name = ?, encrypted_file_hash =?, "+
        "new_file_size = ?, status = 'encrypted', encrypted_at = NOW(), decrypted_at = NULL WHERE og_file_hash = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, fileInfo.getNew_file_name());
            statement.setString(2, fileInfo.getEncrypted_file_hash());
            statement.setLong(3, fileInfo.getNew_file_size());
            statement.setString(4, fileInfo.getOg_file_hash());
            statement.setInt(5, fileInfo.getUser_id());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("The file record was updated successfully for re-encryption!");
            } else {
                System.out.println("Failed to update the file record for re-encryption.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void updateForDecryption(FileInfo fileInfo) {
        String query = "UPDATE files SET status = 'decrypted', decrypted_at = NOW() , new_file_name = ?, " +
                "new_file_size = ?, encrypted_file_hash = NULL WHERE encrypted_file_hash = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, fileInfo.getNew_file_name());
            statement.setLong(2, fileInfo.getNew_file_size());
            statement.setString(3, fileInfo.getEncrypted_file_hash());
            statement.setInt(4, fileInfo.getUser_id());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("The file record was updated successfully for decryption!\n");
            } else {
                System.out.println("Failed to update the file record for decryption.\n");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public String checkFileExists(String og_file_hash, int userId) {
        String query = "SELECT status FROM files WHERE og_file_hash = ? AND user_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, og_file_hash);
            statement.setInt(2, userId);

            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getString("status");
            } else {
                return "not_found";
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validityEncryption(String encrypted_file_hash, int userId) {
        String query = "SELECT status FROM files WHERE encrypted_file_hash = ? AND user_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, encrypted_file_hash);
            statement.setInt(2, userId);

            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String isEncrypted = rs.getString("status");
                return isEncrypted.equals("encrypted");
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public  String getFileHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = fis.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        fis.close();
        byte[] hashBytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    public int getTotalFilesCount() {
        String query = "SELECT COUNT(*) AS total FROM files";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public int getFilesCountByStatus(String status) {
        String query = "SELECT COUNT(*) AS total FROM files WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, status);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Integer> getEncryptedFilesByMonth() {
        Map<String, Integer> monthlyData = new java.util.LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String query = "SELECT MONTH(encrypted_at) as month, COUNT(*) as count " +
                "FROM files " +
                "WHERE YEAR(encrypted_at) = YEAR(CURDATE()) " +
                "GROUP BY MONTH(encrypted_at) " +
                "ORDER BY MONTH(encrypted_at)";

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

        for (String month : months) {
            monthlyData.putIfAbsent(month, 0);
        }

        return monthlyData;
    }

    public Map<String, Integer> getDecryptedFilesByMonth() {
        Map<String, Integer> monthlyData = new java.util.LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String query = "SELECT MONTH(decrypted_at) as month, COUNT(*) as count " +
                "FROM files " +
                "WHERE YEAR(decrypted_at) = YEAR(CURDATE()) AND decrypted_at IS NOT NULL " +
                "GROUP BY MONTH(decrypted_at) " +
                "ORDER BY MONTH(decrypted_at)";

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

        for (String month : months) {
            monthlyData.putIfAbsent(month, 0);
        }

        return monthlyData;
    }

    public Map<String, Integer> getFileTypeDistribution() {
        Map<String, Integer> fileTypes = new java.util.HashMap<>();

        String query = "SELECT og_file_type, COUNT(*) as count FROM files GROUP BY og_file_type";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String fileType = rs.getString("og_file_type");
                int count = rs.getInt("count");

                String category = categorizeFileType(fileType);
                fileTypes.put(category, fileTypes.getOrDefault(category, 0) + count);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return fileTypes;
    }

    private String categorizeFileType(String fileType) {
        if (fileType == null) return "Other";

        fileType = fileType.toLowerCase();
        if (fileType.matches(".*(doc|docx|pdf|txt|xls|xlsx|ppt|pptx).*")) {
            return "Documents";
        } else if (fileType.matches(".*(jpg|jpeg|png|gif|bmp|svg).*")) {
            return "Image";
        } else if (fileType.matches(".*(mp4|avi|mov|wmv|flv|mkv).*")) {
            return "Videos";
        } else {
            return "Other";
        }
    }


}
