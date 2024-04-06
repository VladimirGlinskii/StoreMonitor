package ru.vglinskii.storemonitor.baseapi.projection;

import java.time.Instant;

public interface CashRegisterStatusProjection {
    long getId();

    String getInventoryNumber();

    Instant getOpenedAt();

    Instant getClosedAt();
}