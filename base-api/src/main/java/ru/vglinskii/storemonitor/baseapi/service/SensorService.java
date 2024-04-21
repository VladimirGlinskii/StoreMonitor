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
import ru.vglinskii.storemonitor.baseapi.repository.SensorValueRepository;

@Service
@Slf4j
public class SensorService {
    private final SensorValueRepository sensorValueRepository;
    private final AuthorizationContextHolder authorizationContextHolder;

    public SensorService(
            SensorRepository sensorRepository,
            SensorValueRepository sensorValueRepository, AuthorizationContextHolder authorizationContextHolder
    ) {
        this.sensorValueRepository = sensorValueRepository;
        this.authorizationContextHolder = authorizationContextHolder;
    }

    public List<SensorWithValueDtoResponse> getSensorsWithCurrentValue() {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        var lastSensorsValues = sensorValueRepository.findLastForSensorsByStoreId(storeId);
        log.info("Received get sensors request for store {}", storeId);

        return lastSensorsValues.stream()
                .map((sv) -> SensorWithValueDtoResponse.builder()
                        .id(sv.getSensor().getId())
                        .inventoryNumber(sv.getSensor().getInventoryNumber())
                        .factoryCode(sv.getSensor().getFactoryCode())
                        .location(sv.getSensor().getLocation())
                        .value(SensorValueDtoResponse.builder()
                                .value(sv.getValue())
                                .unit(sv.getUnit())
                                .datetime(sv.getDatetime())
                                .build()
                        )
                        .build()
                )
                .toList();
    }

    public List<SensorWithValuesDtoResponse> getTemperatureReport(
            Instant from,
            Instant rawTo
    ) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        Instant to = (rawTo.isAfter(Instant.now())) ? Instant.now() : rawTo;
        log.info("Received get temperature report request for store {}", storeId);

        var sensorIdToValuesMap = sensorValueRepository.findByStoreIdInInterval(storeId, from, to)
                .stream()
                .collect(Collectors.groupingBy((sv) -> sv.getSensor().getId()));

        return sensorIdToValuesMap.entrySet().stream()
                .map((entry) -> {
                    var values = entry.getValue();
                    var sensor = values.get(0).getSensor();

                    return SensorWithValuesDtoResponse.builder()
                            .id(sensor.getId())
                            .inventoryNumber(sensor.getInventoryNumber())
                            .factoryCode(sensor.getFactoryCode())
                            .location(sensor.getLocation())
                            .values(values.stream()
                                    .map((value) -> SensorValueDtoResponse.builder()
                                            .value(value.getValue())
                                            .unit(value.getUnit())
                                            .datetime(value.getDatetime())
                                            .build()
                                    )
                                    .toList()
                            )
                            .build();
                })
                .toList();
    }
}
