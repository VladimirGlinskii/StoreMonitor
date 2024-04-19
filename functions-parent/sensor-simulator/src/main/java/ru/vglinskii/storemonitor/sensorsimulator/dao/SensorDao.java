package ru.vglinskii.storemonitor.sensorsimulator.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.sensorsimulator.model.Sensor;

public class SensorDao {
    private DatabaseConnectivity databaseConnectivity;

    public SensorDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public List<Sensor> findAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("SELECT * FROM sensor");
             var resultSet = stmt.executeQuery()
        ) {
            var sensors = new ArrayList<Sensor>();
            while (resultSet.next()) {
                sensors.add(mapResultSetToSensor(resultSet));
            }

            return sensors;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Sensor mapResultSetToSensor(ResultSet resultSet) throws SQLException {
        return Sensor.builder()
                .id(resultSet.getLong("id"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("updated_at").toInstant())
                .build();
    }
}
