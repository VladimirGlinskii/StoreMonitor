package ru.vglinskii.storemonitor.functionscommon.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnectivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectivity.class);
    private final String url;
    private final Properties props;
    private Connection connection;

    public DatabaseConnectivity(String url, Properties props) {
        this.url = url;
        this.props = props;
    }

    public Connection getConnection() {
        boolean isConnectionValid;
        try {
            isConnectionValid = connection != null && connection.isValid(1);
        } catch (SQLException e) {
            isConnectionValid = false;
        }

        if (!isConnectionValid) {
            closeConnection();
            initConnection();
        }

        return connection;
    }

    private void initConnection() {
        try {
            LOGGER.info("Initializing database connection");
            connection = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize database connection", e);
            throw new DatabaseConnectionException("Failed to initialize database connection", e);
        }
    }

    private void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            LOGGER.info("Closing database connection");
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("Failed to close database connection", e);
            throw new DatabaseConnectionException("Failed to close database connection", e);
        }
    }
}
