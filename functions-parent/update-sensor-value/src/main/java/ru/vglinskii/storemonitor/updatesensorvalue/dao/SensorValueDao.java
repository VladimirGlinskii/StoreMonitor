package ru.vglinskii.storemonitor.updatesensorvalue.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.updatesensorvalue.model.SensorValue;

public class SensorValueDao {
    private DatabaseConnectivity databaseConnectivity;

    public SensorValueDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public SensorValue create(SensorValue value) {
        var query = """
                    INSERT INTO sensor_value (datetime,unit,value,sensor_id)
                    VALUES (?,?,?,?)
                """;
        try (var connection = databaseConnectivity.getConnection();
             var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
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
