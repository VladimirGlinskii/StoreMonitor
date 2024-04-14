package ru.vglinskii.storemonitor.authfunction.service;

import ru.vglinskii.storemonitor.authfunction.dao.EmployeeDao;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;

public class AuthService {
    private EmployeeDao employeeDao;

    public AuthService(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    public AuthorizationContextDto authorize(String secretKey) {
        var employeeOptional = employeeDao.findBySecret(secretKey);

        return employeeOptional
                .map(employee -> new AuthorizationContextDto(
                                employee.getStoreId(),
                                employee.getId()
                        )
                )
                .orElse(null);

    }
}
