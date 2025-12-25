package com.example.cryptofile;


import javafx.scene.chart.PieChart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public UserDAO() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.print("Connected to database successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static UserInfo loginVerify(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);) {
                statement.setString(1, username);
                statement.setString(2, password);

            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                int id = rs.getInt("user_id");
                String user = rs.getString("username");
                String pass = rs.getString("password");
                String role = rs.getString("roles");
                return new UserInfo(id, user, pass, role);
            } else {
                return null;
            }
        }
    }





}
