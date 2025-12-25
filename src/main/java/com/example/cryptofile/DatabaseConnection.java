package com.example.cryptofile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String URL;
    private static final String user;
    private static final String password;

    static {
        try (var input = Files.newInputStream(Path.of("cryptofile.properties"), StandardOpenOption.READ)) {
            Properties prop = new Properties();
            prop.load(input);

            user = prop.getProperty("databaseUser");
            password = System.getenv("MYSQL_PASS");
            URL = "jdbc:mysql://" + prop.getProperty("serverName") + ":" + prop.getProperty("serverPort") + "/" + prop.getProperty("databaseName");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load database properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, user, password);
    }

}