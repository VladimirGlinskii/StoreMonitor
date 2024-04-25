package ru.vglinskii.storemonitor.decommissionedreportsimulator.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonStoreDao;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.Store;

public class StoreDao extends CommonStoreDao {
    public StoreDao(DatabaseConnectivity databaseConnectivity) {
        super(databaseConnectivity);
    }

    public List<Store> findAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("SELECT * FROM store");
             var resultSet = stmt.executeQuery()
        ) {
            var stores = new ArrayList<Store>();
            while (resultSet.next()) {
                stores.add(mapResultSetToStore(resultSet));
            }

            return stores;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Store mapResultSetToStore(ResultSet resultSet) throws SQLException {
        return Store.builder()
                .id(resultSet.getLong("id"))
                .location(resultSet.getString("location"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("updated_at").toInstant())
                .build();
    }
}
