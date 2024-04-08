package ru.vglinskii.storemonitor.common.dto;

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
}
