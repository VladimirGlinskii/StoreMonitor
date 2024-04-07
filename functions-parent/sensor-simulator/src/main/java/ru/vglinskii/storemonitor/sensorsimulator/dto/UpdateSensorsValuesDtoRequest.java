package ru.vglinskii.storemonitor.sensorsimulator.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateSensorsValuesDtoRequest {
    private List<UpdateSensorValueDtoRequest> values;
}
