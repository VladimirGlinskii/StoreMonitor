package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValuesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.repository.SensorRepository;

@Service
public class SensorService {
    private final SensorRepository sensorRepository;

    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    public List<SensorWithValueDtoResponse> getSensorsWithCurrentValue(long storeId) {
        var sensors = sensorRepository.findByStoreIdWithLastValue(storeId);

        return sensors.stream()
                .map((sensor) -> SensorWithValueDtoResponse.builder()
                        .id(sensor.getId())
                        .inventoryNumber(sensor.getInventoryNumber())
                        .factoryCode(sensor.getFactoryCode())
                        .location(sensor.getLocation())
                        .value(sensor.getValues().stream()
                                .findFirst()
                                .map((value) -> SensorValueDtoResponse.builder()
                                        .value(value.getValue())
                                        .unit(value.getUnit())
                                        .datetime(value.getDatetime())
                                        .build()
                                )
                                .orElse(null)
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<SensorWithValuesDtoResponse> getTemperatureReport(
            long storeId,
            Instant from,
            Instant rawTo
    ) {
        Instant to = (rawTo.isAfter(Instant.now())) ? Instant.now() : rawTo;

        var sensors = sensorRepository.findByStoreIdInInterval(storeId, from, to);

        return sensors.stream()
                .map((sensor) -> SensorWithValuesDtoResponse.builder()
                        .id(sensor.getId())
                        .inventoryNumber(sensor.getInventoryNumber())
                        .factoryCode(sensor.getFactoryCode())
                        .location(sensor.getLocation())
                        .values(sensor.getValues().stream()
                                .map((value) -> SensorValueDtoResponse.builder()
                                        .value(value.getValue())
                                        .unit(value.getUnit())
                                        .datetime(value.getDatetime())
                                        .build()
                                )
                                .collect(Collectors.toList())
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }
}
