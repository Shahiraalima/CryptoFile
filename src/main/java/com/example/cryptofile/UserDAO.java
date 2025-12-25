package com.example.cryptofile;

import java.sql.*;


public class UserDAO {
    public UserDAO() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.print("Connected to database successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public UserInfo loginVerify(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);) {
                statement.setString(1, username);
                statement.setString(2, password);

            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                String user = rs.getString("username");
                String pass = rs.getString("password");
                String role = rs.getString("roles");
                return new UserInfo(user, pass, role);
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error during login verification!");
            throw new RuntimeException(e);
        }
    }

    public boolean registerUser(UserInfo user) {
        String query = "INSERT INTO users (username, email, password, roles) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

             statement.setString(1, user.getUsername());
             statement.setString(2, user.getEmail());
             statement.setString(3, user.getPassword());
             statement.setString(4, user.getRole());

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    return true;
                }
        } catch(SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("Registration failed: Username already exists");
        }
        catch (SQLException e) {
            throw new RuntimeException("Error during registration", e);
        }

        return false;
    }


    public boolean checkUsernameExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking username existence", e);
        }
        return false;
    }

    public boolean checkEmailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking email existence", e);
        }
        return false;
    }

    public String checkPasswordStrength(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isDigit(ch)) hasDigit = true;
            else if ("!@#$%^&*()-+".indexOf(ch) >= 0) hasSpecial = true;
        }
        if(!hasUpper) return "Add at least one uppercase letter.";
        if(!hasLower) return "Add at least one lowercase letter.";
        if(!hasDigit) return "Add at least one digit.";
        if(!hasSpecial) return "Add at least one special character (!@#$%^&*()-+).";
        return "Strong";
    }
}
