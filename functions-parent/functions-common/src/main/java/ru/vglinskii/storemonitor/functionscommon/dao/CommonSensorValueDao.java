package ru.vglinskii.storemonitor.functionscommon.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.SensorValue;

public class CommonSensorValueDao {
    private DatabaseConnectivity databaseConnectivity;

    public CommonSensorValueDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public void deleteAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("DELETE FROM sensor_value")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public SensorValue insert(SensorValue value) {
        var connection = databaseConnectivity.getConnection();
        var query = """
                    INSERT INTO sensor_value (datetime,unit,value,sensor_id)
                    VALUES (?,?,?,?)
                """;
        try (var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.from(value.getDatetime()));
            stmt.setString(2, value.getUnit().name());
            stmt.setFloat(3, value.getValue());
            stmt.setLong(4, value.getSensorId());
            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                var id = generatedKeys.getLong(1);
                value.setId(id);
            }

            return value;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
