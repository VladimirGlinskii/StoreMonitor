package ru.vglinskii.storemonitor.cashiersimulator.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.vglinskii.storemonitor.functionscommon.model.BaseEntity;

@SuperBuilder
@Getter
@Setter
public class CashRegister extends BaseEntity {
    private long storeId;
    private List<CashRegisterSession> daySessions;

    public CashRegisterSession getLastSession() {
        return daySessions.isEmpty()
                ? null
                : daySessions.getLast();
    }
}
