package ru.vglinskii.storemonitor.common.dto;

import java.util.Objects;

public class AuthorizationContextDto {
    private Long storeId;
    private Long employeeId;

    public AuthorizationContextDto() {
        this(null, null);
    }

    public AuthorizationContextDto(Long storeId, Long employeeId) {
        this.storeId = storeId;
        this.employeeId = employeeId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationContextDto that = (AuthorizationContextDto) o;
        return Objects.equals(storeId, that.storeId) && Objects.equals(employeeId, that.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, employeeId);
    }
}
