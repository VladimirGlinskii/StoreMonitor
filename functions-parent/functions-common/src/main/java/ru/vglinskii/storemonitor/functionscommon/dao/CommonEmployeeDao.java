package ru.vglinskii.storemonitor.functionscommon.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.Employee;

public class CommonEmployeeDao {
    protected DatabaseConnectivity databaseConnectivity;

    public CommonEmployeeDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public void deleteAll() {
        var connection = databaseConnectivity.getConnection();
        try (var stmt = connection.prepareStatement("DELETE FROM employee")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Employee insert(Employee employee) {
        var connection = databaseConnectivity.getConnection();
        var query = """
                INSERT INTO employee(first_name,last_name,secret,type,store_id,created_at,updated_at)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (var stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            var now = Timestamp.from(Instant.now());
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getSecret());
            stmt.setString(4, employee.getType().name());
            stmt.setLong(5, employee.getStoreId());
            stmt.setTimestamp(6, (employee.getCreatedAt() == null)
                    ? now
                    : Timestamp.from(employee.getCreatedAt())
            );
            stmt.setTimestamp(7, now);
            stmt.executeUpdate();

            try (var generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                var id = generatedKeys.getLong(1);
                employee.setId(id);
            }

            return employee;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    protected Employee mapResultSetToEmployee(ResultSet resultSet) throws SQLException {
        return Employee.builder()
                .id(resultSet.getLong("id"))
                .firstName(resultSet.getString("first_name"))
                .lastName(resultSet.getString("last_name"))
                .secret(resultSet.getString("secret"))
                .type(EmployeeType.valueOf(resultSet.getString("type")))
                .storeId(resultSet.getLong("store_id"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("updated_at").toInstant())
                .build();
    }
}
