package ru.vglinskii.storemonitor.decommissionedreportsimulator.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.model.DecommissionedReport;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;

public class DecommissionedReportDao {
    private DatabaseConnectivity databaseConnectivity;

    public DecommissionedReportDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public DecommissionedReport create(DecommissionedReport report) {
        var query = """
                    INSERT INTO report (created_at,link,store_id)
                    VALUES (?,?,?)
                """;
        try (var connection = databaseConnectivity.getConnection();
             var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setTimestamp(1, Timestamp.from(report.getCreatedAt()));
            stmt.setString(2, report.getLink());
            stmt.setLong(3, report.getStoreId());
            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                var id = generatedKeys.getLong(1);
                report.setId(id);
            }

            return report;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
