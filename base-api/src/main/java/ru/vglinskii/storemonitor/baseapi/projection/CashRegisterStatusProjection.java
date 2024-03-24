package ru.vglinskii.storemonitor.baseapi.projection;

import java.time.LocalDateTime;

public interface CashRegisterStatusProjection {
    long getId();

    String getInventoryNumber();

    LocalDateTime getOpenedAt();

    LocalDateTime getClosedAt();
}