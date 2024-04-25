package ru.vglinskii.storemonitor.functionscommon.model;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class CashRegisterSession extends BaseEntity {
    private Instant closedAt;
    private long cashierId;
    private long cashRegisterId;
}
