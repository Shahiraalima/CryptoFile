package com.example.cryptofile;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class FileDAO {

    public FileDAO() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.print("Connected to database successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public void insertFile(FileInfo fileInfo) {
        String query = "INSERT INTO files " +
                "(user_id, og_file_name, og_file_size, og_file_type, og_file_hash, " +
                "encrypted_file_name, encrypted_file_size, encrypted_file_hash, encrypted_at, decrypted_at)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NULL)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, fileInfo.getUser_id());
            statement.setString(2, fileInfo.getOg_file_name());
            statement.setLong(3, fileInfo.getOg_file_size());
            statement.setString(4, fileInfo.getOg_file_type());
            statement.setString(5, fileInfo.getOg_file_hash());
            statement.setString(6, fileInfo.getEncrypted_file_name());
            statement.setLong(7, fileInfo.getEncrypted_file_size());
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
        String query = "UPDATE files SET encrypted_file_name = ?, encrypted_file_hash =?, "+
        "encrypted_file_size = ?, status = 'encrypted', encrypted_at = NOW(), decrypted_at = NULL WHERE og_file_hash = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, fileInfo.getEncrypted_file_name());
            statement.setString(2, fileInfo.getEncrypted_file_hash());
            statement.setLong(3, fileInfo.getEncrypted_file_size());
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
        String query = "UPDATE files SET status = 'decrypted', decrypted_at = NOW() , encrypted_file_name = ?, " +
                "encrypted_file_size = ?, encrypted_file_hash = NULL WHERE encrypted_file_hash = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, fileInfo.getEncrypted_file_name());
            statement.setLong(2, fileInfo.getEncrypted_file_size());
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



}
