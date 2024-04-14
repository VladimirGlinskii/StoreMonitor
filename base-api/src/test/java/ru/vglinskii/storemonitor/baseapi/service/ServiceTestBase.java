package ru.vglinskii.storemonitor.baseapi.service;

import org.mockito.Mock;
import org.mockito.Mockito;
import ru.vglinskii.storemonitor.baseapi.TestBase;
import ru.vglinskii.storemonitor.baseapi.auth.AuthorizationContextHolder;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.baseapi.utils.TestDataGenerator;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

public class ServiceTestBase extends TestBase {
    protected TestDataGenerator testDataGenerator;
    protected Store testStore;
    protected Employee testDirector;
    @Mock
    private AuthorizationContextHolder authorizationContextHolder;

    public ServiceTestBase() {
        this.testDataGenerator = new TestDataGenerator();
        this.testStore = testDataGenerator.createStore(1);
        this.testDirector = testDataGenerator.createEmployee(10, testStore, EmployeeType.DIRECTOR);
    }

    protected void authorizeAs(Employee employee) {
        Mockito.when(authorizationContextHolder.getContext())
                .thenReturn(new AuthorizationContextDto(
                        employee.getStore().getId(),
                        employee.getId()
                ));
    }
}
