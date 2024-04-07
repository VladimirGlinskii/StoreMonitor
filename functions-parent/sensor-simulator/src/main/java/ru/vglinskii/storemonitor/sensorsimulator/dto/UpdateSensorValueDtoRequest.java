package ru.vglinskii.storemonitor.sensorsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;

@Data
@AllArgsConstructor
public class UpdateSensorValueDtoRequest {
    private Long sensorId;
    private Float value;
    private SensorUnit unit;
}
