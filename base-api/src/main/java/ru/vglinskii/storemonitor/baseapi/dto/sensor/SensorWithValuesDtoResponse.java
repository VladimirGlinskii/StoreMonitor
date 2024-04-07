package ru.vglinskii.storemonitor.baseapi.dto.sensor;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorWithValuesDtoResponse {
    private long id;
    private String inventoryNumber;
    private String factoryCode;
    private String location;
    private List<SensorValueDtoResponse> values;
}
