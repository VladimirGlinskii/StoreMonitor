package ru.vglinskii.storemonitor.functionscommon.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class Sensor extends BaseEntity {
    private long storeId;
    private String inventoryNumber;
    private String factoryCode;
    private String location;
}
