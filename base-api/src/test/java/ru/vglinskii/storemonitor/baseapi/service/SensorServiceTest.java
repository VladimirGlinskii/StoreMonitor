package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorsWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.mapper.SensorValueMapper;
import ru.vglinskii.storemonitor.baseapi.mapper.SensorValueMapperImpl;
import ru.vglinskii.storemonitor.baseapi.repository.SensorValueRepository;

@ExtendWith(MockitoExtension.class)
public class SensorServiceTest extends ServiceTestBase {
    @Mock
    private SensorValueRepository sensorValueRepository;
    @Spy
    private SensorValueMapper sensorValueMapper = new SensorValueMapperImpl();
    @InjectMocks
    private SensorService sensorService;

    public SensorServiceTest() {
        super();
    }

    @Test
    void getSensorsWithCurrentValue_shouldReturnCorrectResponse() {
        authorizeAs(testDirector);
        var sensor1 = testDataGenerator.createSensor(1, testStore);
        var sensor2 = testDataGenerator.createSensor(2, testStore);
        var sensor1Value = testDataGenerator.createSensorValue(1, sensor1, Instant.now());
        var sensor2Value = testDataGenerator.createSensorValue(2, sensor2, Instant.now());
        var expectedResponse = new SensorsWithValueDtoResponse(
                List.of(
                        sensorValueMapper.toSensorWithValueDto(sensor1Value),
                        sensorValueMapper.toSensorWithValueDto(sensor2Value)

                )
        );

        Mockito.when(sensorValueRepository.findLastForSensorsByStoreId(testStore.getId()))
                .thenReturn(List.of(sensor1Value, sensor2Value));

        var response = sensorService.getSensorsWithCurrentValue();

        Assertions.assertEquals(expectedResponse, response);
    }
}
