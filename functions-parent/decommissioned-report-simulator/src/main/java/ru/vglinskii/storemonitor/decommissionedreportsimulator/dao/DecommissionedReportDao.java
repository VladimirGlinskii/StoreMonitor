package ru.vglinskii.storemonitor.decommissionedreportsimulator.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.model.DecommissionedReport;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;

public class DecommissionedReportDao {
    private DatabaseConnectivity databaseConnectivity;

    public DecommissionedReportDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public void deleteAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("DELETE FROM report")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public DecommissionedReport create(DecommissionedReport report) {
        var connection = databaseConnectivity.getConnection();
        var query = """
                    INSERT INTO report (created_at,link,store_id)
                    VALUES (?,?,?)
                """;
        try (var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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

    public List<DecommissionedReport> findAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("SELECT * FROM report");
             var resultSet = stmt.executeQuery()
        ) {
            var reports = new ArrayList<DecommissionedReport>();
            while (resultSet.next()) {
                reports.add(mapResultSetToReport(resultSet));
            }

            return reports;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private DecommissionedReport mapResultSetToReport(ResultSet resultSet) throws SQLException {
        return DecommissionedReport.builder()
                .id(resultSet.getLong("id"))
                .link(resultSet.getString("link"))
                .storeId(resultSet.getLong("store_id"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .build();
    }
}
