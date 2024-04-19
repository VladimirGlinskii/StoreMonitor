package ru.vglinskii.storemonitor.createincident.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import ru.vglinskii.storemonitor.createincident.model.Incident;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;

public class IncidentDao {
    private DatabaseConnectivity databaseConnectivity;

    public IncidentDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
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
