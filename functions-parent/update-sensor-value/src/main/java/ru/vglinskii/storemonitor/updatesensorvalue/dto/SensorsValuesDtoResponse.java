package ru.vglinskii.storemonitor.updatesensorvalue.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorsValuesDtoResponse {
    private List<SensorValueDtoResponse> values;
}
