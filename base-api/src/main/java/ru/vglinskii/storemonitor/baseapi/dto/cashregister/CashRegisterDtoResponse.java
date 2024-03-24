package ru.vglinskii.storemonitor.baseapi.dto.cashregister;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
