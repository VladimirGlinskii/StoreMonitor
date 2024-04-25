package ru.vglinskii.storemonitor.authfunction.dao;

import java.sql.SQLException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonEmployeeDao;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.Employee;

@Slf4j
public class EmployeeDao extends CommonEmployeeDao {
    public EmployeeDao(DatabaseConnectivity databaseConnectivity) {
        super(databaseConnectivity);
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
}
