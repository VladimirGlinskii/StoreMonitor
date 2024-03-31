package ru.vglinskii.storemonitor.common.dto;

public class AuthorizationContextDto {
    private long storeId;
    private long employeeId;

    public AuthorizationContextDto(long storeId, long employeeId) {
        this.storeId = storeId;
        this.employeeId = employeeId;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }
}
