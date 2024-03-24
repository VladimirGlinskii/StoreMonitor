package ru.vglinskii.storemonitor.baseapi.projection;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CashRegisterStatusProjectionTestImpl implements CashRegisterStatusProjection {
    private long id;
    private String inventoryNumber;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getInventoryNumber() {
        return inventoryNumber;
    }

    @Override
    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    @Override
    public LocalDateTime getClosedAt() {
        return closedAt;
    }
}
