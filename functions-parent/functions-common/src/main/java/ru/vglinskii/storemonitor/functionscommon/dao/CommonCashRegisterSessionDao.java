package ru.vglinskii.storemonitor.functionscommon.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegisterSession;

public class CommonCashRegisterSessionDao {
    protected DatabaseConnectivity databaseConnectivity;

    public CommonCashRegisterSessionDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public void deleteAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("DELETE FROM cash_register_session")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public CashRegisterSession insert(CashRegisterSession session) {
        var connection = databaseConnectivity.getConnection();
        var query = """
                INSERT INTO cash_register_session(cashier_id,cash_register_id,closed_at,created_at,updated_at)
                VALUES (?,?,?,?,?)
                """;
        try (var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            var now = Timestamp.from(Instant.now());
            stmt.setLong(1, session.getCashierId());
            stmt.setLong(2, session.getCashRegisterId());
            stmt.setTimestamp(3, Optional.ofNullable(session.getClosedAt())
                    .map(Timestamp::from)
                    .orElse(null)
            );
            stmt.setTimestamp(4, (session.getCreatedAt() == null)
                    ? now
                    : Timestamp.from(session.getCreatedAt())
            );
            stmt.setTimestamp(5, now);
            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                var id = generatedKeys.getLong(1);
                session.setId(id);
            }

            return session;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
