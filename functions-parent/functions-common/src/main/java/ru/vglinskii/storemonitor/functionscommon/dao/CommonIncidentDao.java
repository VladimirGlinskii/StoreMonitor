package ru.vglinskii.storemonitor.functionscommon.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.Incident;

public class CommonIncidentDao {
    protected DatabaseConnectivity databaseConnectivity;

    public CommonIncidentDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public void deleteAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("DELETE FROM incident")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Incident create(Incident incident) {
        var connection = databaseConnectivity.getConnection();
        var query = """
                    INSERT INTO incident (called_ambulance,called_fire_department,called_gas_service,called_police,datetime,description,event_type,store_id)
                    VALUES (?,?,?,?,?,?,?,?)
                """;
        try (var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setBoolean(1, incident.isCalledAmbulance());
            stmt.setBoolean(2, incident.isCalledFireDepartment());
            stmt.setBoolean(3, incident.isCalledGasService());
            stmt.setBoolean(4, incident.isCalledPolice());
            stmt.setTimestamp(5, Timestamp.from(incident.getDatetime()));
            stmt.setString(6, incident.getDescription());
            stmt.setString(7, incident.getEventType().name());
            stmt.setLong(8, incident.getStoreId());
            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                var id = generatedKeys.getLong(1);
                incident.setId(id);
            }

            return incident;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
