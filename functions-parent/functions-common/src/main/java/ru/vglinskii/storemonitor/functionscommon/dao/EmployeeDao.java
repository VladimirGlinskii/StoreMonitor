package ru.vglinskii.storemonitor.functionscommon.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.Employee;

public class EmployeeDao {
    private DatabaseConnectivity databaseConnectivity;

    public EmployeeDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public Optional<Employee> findBySecretAndStoreId(String secret, long storeId) {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("SELECT * FROM employee WHERE secret = ? AND store_id = ?")) {
            stmt.setString(1, secret);
            stmt.setLong(2, storeId);
            try (var resultSet = stmt.executeQuery()) {
                return (resultSet.next())
                        ? Optional.of(mapResultSetToFolder(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Employee mapResultSetToFolder(ResultSet resultSet) throws SQLException {
        return Employee.builder()
                .id(resultSet.getLong("id"))
                .secret(resultSet.getString("secret"))
                .firstName(resultSet.getString("first_name"))
                .lastName(resultSet.getString("last_name"))
                .storeId(resultSet.getLong("store_id"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
