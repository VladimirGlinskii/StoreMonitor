package ru.vglinskii.storemonitor.baseapi.utils;

import java.time.LocalDateTime;
import ru.vglinskii.storemonitor.baseapi.model.CashRegister;
import ru.vglinskii.storemonitor.baseapi.model.CashRegisterSession;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

public class TestDataGenerator {
    public Store createStore(long id) {
        var now = LocalDateTime.now();

        return Store.builder()
                .id(id)
                .location(String.format("Location %d", id))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public CashRegister createCashRegister(long id, Store store) {
        var now = LocalDateTime.now();

        return CashRegister.builder()
                .id(id)
                .inventoryNumber(String.valueOf(id))
                .store(store)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public CashRegisterSession createCashRegisterSession(
            long id,
            CashRegister cashRegister,
            Employee cashier,
            LocalDateTime closedAt
    ) {
        var now = LocalDateTime.now();

        return CashRegisterSession.builder()
                .id(id)
                .cashRegister(cashRegister)
                .cashier(cashier)
                .createdAt(now)
                .updatedAt(now)
                .closedAt(closedAt)
                .build();
    }

    public CashRegisterSession createCashRegisterSession(long id, CashRegister cashRegister, Employee cashier) {
        return createCashRegisterSession(id, cashRegister, cashier, null);
    }

    public Employee createEmployee(long id, Store store, EmployeeType type) {
        var now = LocalDateTime.now();

        return Employee.builder()
                .id(id)
                .firstName(String.format("%sFirstName %d", type.name(), id))
                .lastName(String.format("%sLastName %d", type.name(), id))
                .secret(String.format("secret%d", id))
                .store(store)
                .type(type)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
