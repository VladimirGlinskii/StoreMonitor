package ru.vglinskii.storemonitor.baseapi.dto.sensor;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorValueDtoResponse {
    private float value;
    private SensorUnit unit;
    private Instant datetime;
}
