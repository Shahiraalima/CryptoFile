package com.example.cryptofile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserInfo {
    private int user_id;
    private String username;
    private String email;
    private String password;
    private String role;
    private String fullName;
    private LocalDateTime account_created;

    public UserInfo() {
    }

    // Constructor for login verification
    public UserInfo(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDateTime getAccount_created() {
        return account_created;
    }

    public void setAccount_created(LocalDateTime account_created) {
        this.account_created = account_created;
    }

    public String dateFormatter() {
        return account_created.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"));
    }

}
