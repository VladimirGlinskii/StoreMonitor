package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.repository.SensorValueRepository;

@ExtendWith(MockitoExtension.class)
public class SensorServiceTest extends ServiceTestBase {
    @Mock
    private SensorValueRepository sensorValueRepository;
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
        var expectedResponse = List.of(
                SensorWithValueDtoResponse.builder()
                        .id(sensor1.getId())
                        .inventoryNumber(sensor1.getInventoryNumber())
                        .factoryCode(sensor1.getFactoryCode())
                        .location(sensor1.getLocation())
                        .value(SensorValueDtoResponse.builder()
                                .datetime(sensor1Value.getDatetime())
                                .unit(sensor1Value.getUnit())
                                .value(sensor1Value.getValue())
                                .build()
                        )
                        .build(),
                SensorWithValueDtoResponse.builder()
                        .id(sensor2.getId())
                        .inventoryNumber(sensor2.getInventoryNumber())
                        .factoryCode(sensor2.getFactoryCode())
                        .location(sensor2.getLocation())
                        .value(SensorValueDtoResponse.builder()
                                .datetime(sensor2Value.getDatetime())
                                .unit(sensor2Value.getUnit())
                                .value(sensor2Value.getValue())
                                .build()
                        )
                        .build()
        );

        Mockito.when(sensorValueRepository.findLastForSensorsByStoreId(testStore.getId()))
                .thenReturn(List.of(sensor1Value, sensor2Value));

        var response = sensorService.getSensorsWithCurrentValue();

        Assertions.assertEquals(expectedResponse, response);
    }
}
