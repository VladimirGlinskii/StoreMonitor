package ru.vglinskii.storemonitor.incidentsreport.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.incidentsreport.model.Incident;

public class IncidentDao {
    private DatabaseConnectivity databaseConnectivity;

    public IncidentDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public List<Incident> findByStoreIdInInterval(long storeId, Instant from, Instant to) {
        var query = """
                    SELECT * FROM incident
                    WHERE store_id = ? AND datetime >= ? AND datetime <= ?
                """;
        try (var connection = databaseConnectivity.getConnection();
             var stmt = connection.prepareStatement(query)
        ) {
            stmt.setLong(1, storeId);
            stmt.setTimestamp(2, Timestamp.from(from));
            stmt.setTimestamp(3, Timestamp.from(to));

            try (var rs = stmt.executeQuery()) {
                var incidents = new ArrayList<Incident>();
                while (rs.next()) {
                    incidents.add(mapResultSetToIncident(rs));
                }

                return incidents;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Incident mapResultSetToIncident(ResultSet rs) throws SQLException {
        return Incident.builder()
                .id(rs.getLong("id"))
                .calledAmbulance(rs.getBoolean("called_ambulance"))
                .calledFireDepartment(rs.getBoolean("called_fire_department"))
                .calledGasService(rs.getBoolean("called_gas_service"))
                .calledPolice(rs.getBoolean("called_police"))
                .build();
    }
}