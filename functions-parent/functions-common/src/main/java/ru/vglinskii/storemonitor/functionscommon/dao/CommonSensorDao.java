package ru.vglinskii.storemonitor.functionscommon.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.Sensor;

public class CommonSensorDao {
    protected DatabaseConnectivity databaseConnectivity;

    public CommonSensorDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public void deleteAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("DELETE FROM sensor")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Sensor insert(Sensor sensor) {
        var connection = databaseConnectivity.getConnection();
        var query = """
                INSERT INTO sensor(store_id,inventory_number,factory_code,location,created_at,updated_at)
                VALUES (?,?,?,?,?,?)
                """;
        try (var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            var now = Timestamp.from(Instant.now());
            stmt.setLong(1, sensor.getStoreId());
            stmt.setString(2, sensor.getInventoryNumber());
            stmt.setString(3, sensor.getFactoryCode());
            stmt.setString(4, sensor.getLocation());
            stmt.setTimestamp(5, Optional.ofNullable(sensor.getCreatedAt())
                    .map(Timestamp::from)
                    .orElse(now)
            );
            stmt.setTimestamp(6, now);
            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                var id = generatedKeys.getLong(1);
                sensor.setId(id);
            }

            return sensor;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
