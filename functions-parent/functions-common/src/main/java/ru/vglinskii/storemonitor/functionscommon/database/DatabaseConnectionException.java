package ru.vglinskii.storemonitor.functionscommon.database;

public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
