package com.library.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Loads database connection settings from db.properties (on the classpath,
 * e.g. src/main/resources/db.properties) and hands out JDBC connections.
 *
 * Expected properties:
 *   db.url=jdbc:postgresql://localhost:5432/library
 *   db.user=postgres
 *   db.password=yourpassword
 */
public final class DatabaseConfig {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException(
                    "db.properties not found on classpath. " +
                    "Copy src/main/resources/db.properties.example to db.properties and fill in your credentials.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
    }

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
