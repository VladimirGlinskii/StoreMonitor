package ru.vglinskii.storemonitor.baseapi.mapper;

import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValuesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.model.Sensor;
import ru.vglinskii.storemonitor.baseapi.model.SensorValue;

@Mapper
public abstract class SensorValueMapper {
    public abstract SensorValueDtoResponse toValueDto(SensorValue value);

    public abstract List<SensorValueDtoResponse> toValueDtos(Collection<SensorValue> values);

    public SensorWithValueDtoResponse toSensorWithValueDto(SensorValue value) {
        return SensorWithValueDtoResponse.builder()
                .id(value.getSensor().getId())
                .inventoryNumber(value.getSensor().getInventoryNumber())
                .factoryCode(value.getSensor().getFactoryCode())
                .location(value.getSensor().getLocation())
                .value(toValueDto(value))
                .build();
    }

    public SensorWithValuesDtoResponse toSensorWithValuesDto(List<SensorValue> values, Sensor sensor) {
        return SensorWithValuesDtoResponse.builder()
                .id(sensor.getId())
                .inventoryNumber(sensor.getInventoryNumber())
                .factoryCode(sensor.getFactoryCode())
                .location(sensor.getLocation())
                .values(toValueDtos(values))
                .build();
    }
}
