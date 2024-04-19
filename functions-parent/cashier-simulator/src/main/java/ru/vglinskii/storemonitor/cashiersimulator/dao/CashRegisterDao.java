package ru.vglinskii.storemonitor.cashiersimulator.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ru.vglinskii.storemonitor.cashiersimulator.model.CashRegister;
import ru.vglinskii.storemonitor.cashiersimulator.model.CashRegisterSession;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;

public class CashRegisterDao {
    private DatabaseConnectivity databaseConnectivity;

    public CashRegisterDao(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
    }

    public List<CashRegister> findAllWithDaySessions() {
        var connection = databaseConnectivity.getConnection();
        var query = """
                SELECT
                    cr.*,
                    s.id as s_id,
                    s.created_at as s_created_at,
                    s.closed_at as s_closed_at,
                    s.cashier_id as s_cashier_id
                FROM cash_register cr
                LEFT JOIN cash_register_session s
                ON s.cash_register_id = cr.id AND (s.closed_at is null OR s.created_at >= UTC_DATE())
                ORDER BY cr.id ASC, s.created_at ASC
                """;
        try (var stmt = connection.prepareStatement(query);
             var resultSet = stmt.executeQuery()
        ) {
            var registers = new ArrayList<CashRegister>();
            CashRegister prevRegister = null;

            while (resultSet.next()) {
                var register = mapResultSetToCashRegister(resultSet);
                var session = mapResultSetToCashRegisterSession(resultSet);
                if (prevRegister == null || !Objects.equals(prevRegister.getId(), register.getId())) {
                    registers.add(register);
                    prevRegister = register;
                }
                if (session != null) {
                    prevRegister.getDaySessions().addLast(session);
                }
            }

            return registers;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private CashRegister mapResultSetToCashRegister(ResultSet resultSet) throws SQLException {
        return CashRegister.builder()
                .id(resultSet.getLong("id"))
                .storeId(resultSet.getLong("store_id"))
                .daySessions(new ArrayList<>())
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("updated_at").toInstant())
                .build();
    }

    private CashRegisterSession mapResultSetToCashRegisterSession(ResultSet resultSet) throws SQLException {
        if (resultSet.getObject("s_id") == null) {
            return null;
        }

        return CashRegisterSession.builder()
                .id(resultSet.getLong("s_id"))
                .createdAt(resultSet.getTimestamp("s_created_at").toInstant())
                .closedAt(
                        Optional.ofNullable(resultSet.getTimestamp("s_closed_at"))
                                .map(Timestamp::toInstant)
                                .orElse(null)
                )
                .cashierId(resultSet.getLong("s_cashier_id"))
                .build();
    }
}
