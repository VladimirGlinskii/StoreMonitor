package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vglinskii.storemonitor.baseapi.auth.AuthorizationContextHolder;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValuesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.repository.SensorRepository;

@Service
@Slf4j
public class SensorService {
    private final SensorRepository sensorRepository;
    private final AuthorizationContextHolder authorizationContextHolder;

    public SensorService(
            SensorRepository sensorRepository,
            AuthorizationContextHolder authorizationContextHolder
    ) {
        this.sensorRepository = sensorRepository;
        this.authorizationContextHolder = authorizationContextHolder;
    }

    public List<SensorWithValueDtoResponse> getSensorsWithCurrentValue() {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        var sensors = sensorRepository.findByStoreIdWithLastValue(storeId);
        log.info("Received get sensors request for store {}", storeId);

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
            Instant from,
            Instant rawTo
    ) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        Instant to = (rawTo.isAfter(Instant.now())) ? Instant.now() : rawTo;
        log.info("Received get temperature report request for store {}", storeId);

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
