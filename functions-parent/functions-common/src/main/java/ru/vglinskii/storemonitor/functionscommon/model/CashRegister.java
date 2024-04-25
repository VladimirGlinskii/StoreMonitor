package ru.vglinskii.storemonitor.functionscommon.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class CashRegister extends BaseEntity {
    private long storeId;
    private String inventoryNumber;
    private List<CashRegisterSession> sessions;

    public CashRegisterSession getActiveSession() {
        if (sessions.isEmpty() || sessions.getLast().getClosedAt() != null) {
            return null;
        }

        return sessions.getLast();
    }
}
