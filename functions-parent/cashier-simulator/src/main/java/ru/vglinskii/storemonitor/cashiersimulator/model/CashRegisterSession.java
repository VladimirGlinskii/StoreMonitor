package ru.vglinskii.storemonitor.cashiersimulator.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.vglinskii.storemonitor.functionscommon.model.BaseEntity;

@SuperBuilder
@Getter
@Setter
public class CashRegisterSession extends BaseEntity {
    private LocalDateTime closedAt;
    private long cashierId;
}
