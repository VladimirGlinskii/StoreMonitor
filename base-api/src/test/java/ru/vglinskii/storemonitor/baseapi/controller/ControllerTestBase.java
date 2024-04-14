package ru.vglinskii.storemonitor.baseapi.controller;

import ru.vglinskii.storemonitor.baseapi.TestBase;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.baseapi.utils.TestDataGenerator;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

public class ControllerTestBase extends TestBase {
    protected TestDataGenerator testDataGenerator;
    protected Store testStore;
    protected Employee testDirector;
    protected Employee testCashier;

    public ControllerTestBase() {
        this.testDataGenerator = new TestDataGenerator();
        this.testStore = testDataGenerator.createStore(1);
        this.testDirector = testDataGenerator.createEmployee(10, testStore, EmployeeType.DIRECTOR);
        this.testCashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
    }
}
