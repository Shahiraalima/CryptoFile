package com.example.cryptofile;

import javafx.collections.ObservableList;

import java.sql.*;


public class UserDAO {

    public UserDAO() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.print("Connected to database successfully.\n");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database.\n", e);
        }
    }

    public ObservableList<UserInfo> getAllUsers() {
        String query = "SELECT * FROM USERS";
        ObservableList<UserInfo> usersList = javafx.collections.FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);) {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                UserInfo user = new UserInfo();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("roles"));
                user.setFullName(rs.getString("full_name"));
                user.setAccount_created(rs.getTimestamp("account_created").toLocalDateTime());
                usersList.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return usersList;
    }

        // Verify user login credentials and return UserInfo if valid
    public UserInfo loginVerify(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);) {
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String user = rs.getString("username");
                String pass = rs.getString("password");
                String role = rs.getString("roles");
                int user_id = rs.getInt("user_id");

                UserInfo currentUser = new UserInfo();
                currentUser.setUser_id(user_id);
                currentUser.setUsername(user);
                currentUser.setRole(role);
                currentUser.setPassword(pass);
                currentUser.setFullName(rs.getString("full_name"));
                currentUser.setEmail(rs.getString("email"));
                currentUser.setAccount_created(rs.getTimestamp("account_created").toLocalDateTime());
                SessionManager.loggedInUser = currentUser;

                return new UserInfo(user, pass, role);
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error during login verification!");
            throw new RuntimeException(e);
        }
    }

    // Register a new user in the database
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
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("Registration failed: Username already exists");
        } catch (SQLException e) {
            throw new RuntimeException("Error during registration", e);
        }
        return false;
    }

    public boolean updateUserInfo(UserInfo user) {
        String query = "UPDATE users SET full_name = ?, email = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getUsername());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user information", e);
        }
    }


    public boolean updateUserPassword(String username, String newPass) {
        String query = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, newPass);
            statement.setString(2, username);

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user password", e);
        }
    }

    public void deleteUser(String username) {
        String query = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }


    // Check if username already exists in the database
    public boolean checkUsernameExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking username existence", e);
        }
        return false;
    }

    // Check if email already exists in the database
    public boolean checkEmailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking email existence", e);
        }
        return false;
    }

    // Check password strength
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
        if (!hasUpper) return "Add at least one uppercase letter.";
        if (!hasLower) return "Add at least one lowercase letter.";
        if (!hasDigit) return "Add at least one digit.";
        if (!hasSpecial) return "Add at least one special character (!@#$%^&*()-+).";
        return "Strong";
    }


}
