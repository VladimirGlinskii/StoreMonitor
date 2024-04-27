package ru.vglinskii.storemonitor.functionscommon.dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.Store;

public class CommonStoreDao {
    protected DatabaseConnectivity databaseConnectivity;

    public CommonStoreDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public void deleteAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("DELETE FROM store")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Store insert(Store store) {
        var connection = databaseConnectivity.getConnection();
        var query = """
                INSERT INTO store(location,created_at,updated_at)
                VALUES (?,?,?)
                """;
        try (var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            var now = Timestamp.from(Instant.now());
            stmt.setString(1, store.getLocation());
            stmt.setTimestamp(2, (store.getCreatedAt() == null)
                    ? now
                    : Timestamp.from(store.getCreatedAt())
            );
            stmt.setTimestamp(3, now);
            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                var id = generatedKeys.getLong(1);
                store.setId(id);
            }

            return store;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
