package ru.vglinskii.storemonitor.baseapi.utils;

import java.time.Instant;
import java.util.List;
import ru.vglinskii.storemonitor.baseapi.model.CashRegister;
import ru.vglinskii.storemonitor.baseapi.model.CashRegisterSession;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.baseapi.model.Sensor;
import ru.vglinskii.storemonitor.baseapi.model.SensorValue;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;

public class TestDataGenerator {
    public Store createStore(long id) {
        var now = Instant.now();

        return Store.builder()
                .id(id)
                .location(String.format("Location %d", id))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public CashRegister createCashRegister(long id, Store store) {
        var now = Instant.now();

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
            Instant closedAt
    ) {
        var now = Instant.now();

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
        var now = Instant.now();

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

    public Sensor createSensor(long id, Store store, List<SensorValue> values) {
        var now = Instant.now();

        return Sensor.builder()
                .id(id)
                .inventoryNumber(String.valueOf(id))
                .factoryCode(String.valueOf(id))
                .location(String.format("Location %d", id))
                .store(store)
                .values(values)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public SensorValue createSensorValue(long id) {
        return createSensorValue(id, null, Instant.now());
    }

    public SensorValue createSensorValue(long id, Sensor sensor, Instant datetime) {
        return SensorValue.builder()
                .id(id)
                .unit(SensorUnit.CELSIUS)
                .value((float) (Math.random() * 10))
                .datetime(datetime)
                .sensor(sensor)
                .build();
    }
}
