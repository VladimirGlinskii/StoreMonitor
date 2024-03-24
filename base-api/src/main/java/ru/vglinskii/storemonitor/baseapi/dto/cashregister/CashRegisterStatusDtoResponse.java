package ru.vglinskii.storemonitor.baseapi.dto.cashregister;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashRegisterStatusDtoResponse {
    private long id;
    private String inventoryNumber;
    private boolean opened;
}
