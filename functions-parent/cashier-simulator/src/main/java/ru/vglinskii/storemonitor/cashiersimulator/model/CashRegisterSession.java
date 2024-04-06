package ru.vglinskii.storemonitor.cashiersimulator.model;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.vglinskii.storemonitor.functionscommon.model.BaseEntity;

@SuperBuilder
@Getter
@Setter
public class CashRegisterSession extends BaseEntity {
    private Instant closedAt;
    private long cashierId;
}
