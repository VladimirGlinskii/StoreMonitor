package ru.vglinskii.storemonitor.baseapi.projection;

import java.time.Instant;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CashRegisterStatusProjectionTestImpl implements CashRegisterStatusProjection {
    private long id;
    private String inventoryNumber;
    private Instant openedAt;
    private Instant closedAt;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getInventoryNumber() {
        return inventoryNumber;
    }

    @Override
    public Instant getOpenedAt() {
        return openedAt;
    }

    @Override
    public Instant getClosedAt() {
        return closedAt;
    }
}
