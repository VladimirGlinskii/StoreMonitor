package ru.vglinskii.storemonitor.cashiersimulator.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import ru.vglinskii.storemonitor.cashiersimulator.model.Cashier;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;

public class CashierDao {
    private DatabaseConnectivity databaseConnectivity;

    public CashierDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public List<Cashier> findAllOrderedByActivity() {
        var connection = databaseConnectivity.getConnection();
        var query = """
                    SELECT
                        c.*,
                        IFNULL(sessions_info.is_free, true) as is_free,
                        IFNULL(sessions_info.worked_seconds_in_day, 0) as worked_seconds_in_day
                    FROM employee c
                    LEFT JOIN (
                        SELECT
                            cashier_id,
                            (MAX(closed_at is null) != true) as is_free,
                            SUM(TIMESTAMPDIFF(SECOND, created_at, IFNULL(closed_at, ?))) as worked_seconds_in_day
                        FROM cash_register_session s
                        WHERE s.closed_at is null OR s.created_at >= ?
                        GROUP BY cashier_id
                    ) sessions_info ON sessions_info.cashier_id = c.id
                    WHERE type = "CASHIER"
                    ORDER BY worked_seconds_in_day ASC
                """;
        try (var stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.setTimestamp(2, Timestamp.from(Instant.now().truncatedTo(ChronoUnit.DAYS)));
            try (var resultSet = stmt.executeQuery()) {
                var cashiers = new ArrayList<Cashier>();
                while (resultSet.next()) {
                    cashiers.add(mapResultSetToCashier(resultSet));
                }

                return cashiers;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Cashier mapResultSetToCashier(ResultSet resultSet) throws SQLException {
        return Cashier.builder()
                .id(resultSet.getLong("id"))
                .storeId(resultSet.getLong("store_id"))
                .isFree(resultSet.getBoolean("is_free"))
                .workedSecondsInDay(resultSet.getInt("worked_seconds_in_day"))
                .secret(resultSet.getString("secret"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("updated_at").toInstant())
                .build();
    }
}
