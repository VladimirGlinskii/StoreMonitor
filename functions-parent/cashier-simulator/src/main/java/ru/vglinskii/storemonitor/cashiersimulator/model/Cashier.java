package ru.vglinskii.storemonitor.cashiersimulator.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.vglinskii.storemonitor.functionscommon.model.BaseEntity;

@SuperBuilder
@Getter
@Setter
public class Cashier extends BaseEntity {
    private long storeId;
    private boolean isFree;
    private int workedSecondsInDay;
    private String secret;
}
