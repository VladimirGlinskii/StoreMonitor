package ru.vglinskii.storemonitor.authfunction.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import ru.vglinskii.storemonitor.authfunction.model.Employee;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;

@Slf4j
public class EmployeeDao {
    private DatabaseConnectivity databaseConnectivity;

    public EmployeeDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public Optional<Employee> findBySecret(String secret) {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("SELECT * FROM employee WHERE secret = ?")) {
            stmt.setString(1, secret);
            try (var resultSet = stmt.executeQuery()) {
                return (resultSet.next())
                        ? Optional.of(mapResultSetToEmployee(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Employee mapResultSetToEmployee(ResultSet resultSet) throws SQLException {
        return Employee.builder()
                .id(resultSet.getLong("id"))
                .secret(resultSet.getString("secret"))
                .storeId(resultSet.getLong("store_id"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("updated_at").toInstant())
                .build();
    }
}
