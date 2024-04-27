package ru.vglinskii.storemonitor.functionscommon.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class Store extends BaseEntity {
    private String location;
}
