package ru.vglinskii.storemonitor.functionscommon.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;

@Builder
@Getter
@Setter
public class SensorValue {
    private long id;
    private long sensorId;
    private float value;
    private SensorUnit unit;
    private Instant datetime;
}
