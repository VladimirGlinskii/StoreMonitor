package ru.vglinskii.storemonitor.baseapi.dto.cashregister;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashRegisterDtoResponse {
    private long id;
    private String inventoryNumber;
    private boolean opened;
    private Instant createdAt;
    private Instant updatedAt;
}
