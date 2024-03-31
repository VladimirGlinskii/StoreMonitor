package ru.vglinskii.storemonitor.functionscommon.database;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Getter
public class DatabaseConnectivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectivity.class);
    private Connection connection = null;

    public void initConnection(String url, Properties props) {
        if (connection == null) {
            try {
                LOGGER.info("Initializing database connection");
                connection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                LOGGER.error("Failed to initialize database connection", e);
                throw new DatabaseConnectionException("Failed to initialize database connection", e);
            }
        }
    }

    public void close() {
        if (connection != null) {
            try {
                LOGGER.info("Closing database connection");
                connection.close();
                connection = null;
            } catch (SQLException e) {
                LOGGER.error("Failed to close database connection", e);
                throw new DatabaseConnectionException("Failed to close database connection", e);
            }
        }
    }
}
