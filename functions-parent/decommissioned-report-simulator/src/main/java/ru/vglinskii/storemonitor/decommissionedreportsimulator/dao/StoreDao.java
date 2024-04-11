package ru.vglinskii.storemonitor.decommissionedreportsimulator.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.model.Store;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;

public class StoreDao {
    private DatabaseConnectivity databaseConnectivity;

    public StoreDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public List<Store> findAll() {
        try (var connection = databaseConnectivity.getConnection();
             var stmt = connection.prepareStatement("SELECT * FROM store");
             var resultSet = stmt.executeQuery()
        ) {
            var stores = new ArrayList<Store>();
            while (resultSet.next()) {
                stores.add(mapResultSetToSensor(resultSet));
            }

            return stores;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Store mapResultSetToSensor(ResultSet resultSet) throws SQLException {
        return Store.builder()
                .id(resultSet.getLong("id"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("updated_at").toInstant())
                .build();
    }
}
