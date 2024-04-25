package ru.vglinskii.storemonitor.functionscommon.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegister;

public class CommonCashRegisterDao {
    protected DatabaseConnectivity databaseConnectivity;

    public CommonCashRegisterDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public void deleteAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("DELETE FROM cash_register")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public CashRegister insert(CashRegister register) {
        var connection = databaseConnectivity.getConnection();
        var query = """
                INSERT INTO cash_register(store_id,inventory_number,created_at,updated_at)
                VALUES (?,?,?,?)
                """;
        try (var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            var now = Timestamp.from(Instant.now());
            stmt.setLong(1, register.getStoreId());
            stmt.setString(2, register.getInventoryNumber());
            stmt.setTimestamp(3, (register.getCreatedAt() == null)
                    ? now
                    : Timestamp.from(register.getCreatedAt())
            );
            stmt.setTimestamp(4, now);
            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                var id = generatedKeys.getLong(1);
                register.setId(id);
            }

            return register;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
