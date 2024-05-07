package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vglinskii.storemonitor.baseapi.auth.AuthorizationContextHolder;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValuesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.mapper.SensorValueMapper;
import ru.vglinskii.storemonitor.baseapi.repository.SensorValueRepository;

@Service
@Slf4j
public class SensorService {
    private final SensorValueRepository sensorValueRepository;
    private final AuthorizationContextHolder authorizationContextHolder;
    private final SensorValueMapper sensorValueMapper;

    public SensorService(
            SensorValueRepository sensorValueRepository,
            AuthorizationContextHolder authorizationContextHolder,
            SensorValueMapper sensorValueMapper) {
        this.sensorValueRepository = sensorValueRepository;
        this.authorizationContextHolder = authorizationContextHolder;
        this.sensorValueMapper = sensorValueMapper;
    }

    public List<SensorWithValueDtoResponse> getSensorsWithCurrentValue() {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        var lastSensorsValues = sensorValueRepository.findLastForSensorsByStoreId(storeId);
        log.info("Received get sensors request for store {}", storeId);

        return lastSensorsValues.stream()
                .map(sensorValueMapper::toSensorWithValueDto)
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

        return sensorIdToValuesMap.values().stream()
                .map(values -> sensorValueMapper.toSensorWithValuesDto(
                        values,
                        values.get(0).getSensor()
                ))
                .toList();
    }
}
