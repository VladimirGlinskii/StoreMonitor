package ru.vglinskii.storemonitor.authfunction.service;

import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;
import ru.vglinskii.storemonitor.functionscommon.dao.EmployeeDao;

public class AuthService {
    private EmployeeDao employeeDao;

    public AuthService(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    public AuthorizationContextDto authorize(String secretKey, long storeId) {
        var employeeOptional = employeeDao.findBySecretAndStoreId(secretKey, storeId);

        return employeeOptional
                .map(employee -> new AuthorizationContextDto(
                                employee.getStoreId(),
                                employee.getId()
                        )
                )
                .orElse(null);

    }
}
