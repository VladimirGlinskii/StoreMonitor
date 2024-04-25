package ru.vglinskii.storemonitor.updatesensorvalue.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorValueDtoResponse {
    private long id;
    private long sensorId;
    private float value;
    private SensorUnit unit;
    private Instant datetime;
}
