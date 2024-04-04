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

    public DatabaseConnectivity(String url, Properties props) {
        this.url = url;
        this.props = props;
    }

    public Connection getConnection() {
        try {
            LOGGER.info("Initializing database connection");
            return DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize database connection", e);
            throw new DatabaseConnectionException("Failed to initialize database connection", e);
        }
    }
}
