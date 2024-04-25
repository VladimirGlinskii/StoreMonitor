package ru.vglinskii.storemonitor.cashiersimulator.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonCashRegisterDao;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegister;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegisterSession;

public class CashRegisterDao extends CommonCashRegisterDao {
    public CashRegisterDao(DatabaseConnectivity databaseConnectivity) {
        super(databaseConnectivity);
    }

    public List<CashRegister> findAllWithDaySessions() {
        var connection = databaseConnectivity.getConnection();
        var query = """
                SELECT
                    cr.*,
                    s.id as s_id,
                    s.created_at as s_created_at,
                    s.updated_at as s_updated_at,
                    s.closed_at as s_closed_at,
                    s.cashier_id as s_cashier_id,
                    s.cash_register_id as s_cash_register_id
                FROM cash_register cr
                LEFT JOIN cash_register_session s
                ON s.cash_register_id = cr.id AND (s.closed_at is null OR s.created_at >= ?)
                ORDER BY cr.id ASC, s.created_at ASC
                """;
        try (var stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.from(Instant.now().truncatedTo(ChronoUnit.DAYS)));
            try (var resultSet = stmt.executeQuery()) {
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
                        prevRegister.getSessions().addLast(session);
                    }
                }

                return registers;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private CashRegister mapResultSetToCashRegister(ResultSet resultSet) throws SQLException {
        return CashRegister.builder()
                .id(resultSet.getLong("id"))
                .storeId(resultSet.getLong("store_id"))
                .inventoryNumber(resultSet.getString("inventory_number"))
                .createdAt(resultSet.getTimestamp("created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("updated_at").toInstant())
                .sessions(new ArrayList<>())
                .build();
    }

    private CashRegisterSession mapResultSetToCashRegisterSession(ResultSet resultSet) throws SQLException {
        if (resultSet.getObject("s_id") == null) {
            return null;
        }

        return CashRegisterSession.builder()
                .id(resultSet.getLong("s_id"))
                .createdAt(resultSet.getTimestamp("s_created_at").toInstant())
                .updatedAt(resultSet.getTimestamp("s_updated_at").toInstant())
                .closedAt(
                        Optional.ofNullable(resultSet.getTimestamp("s_closed_at"))
                                .map(Timestamp::toInstant)
                                .orElse(null)
                )
                .cashierId(resultSet.getLong("s_cashier_id"))
                .cashRegisterId(resultSet.getLong("s_cash_register_id"))
                .build();
    }
}
