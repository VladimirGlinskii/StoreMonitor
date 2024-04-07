package ru.vglinskii.storemonitor.baseapi.dto.sensor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorWithValueDtoResponse {
    private long id;
    private String inventoryNumber;
    private String factoryCode;
    private String location;
    private SensorValueDtoResponse value;
}
