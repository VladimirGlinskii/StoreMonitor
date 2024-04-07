package ru.vglinskii.storemonitor.baseapi.service;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vglinskii.storemonitor.baseapi.TestBase;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.baseapi.repository.SensorRepository;
import ru.vglinskii.storemonitor.baseapi.utils.TestDataGenerator;

@ExtendWith(MockitoExtension.class)
public class SensorServiceTest extends TestBase {
    private final TestDataGenerator testDataGenerator;
    private final Store testStore;
    @Mock
    private SensorRepository sensorRepository;
    @InjectMocks
    private SensorService sensorService;

    public SensorServiceTest() {
        this.testDataGenerator = new TestDataGenerator();
        this.testStore = testDataGenerator.createStore(1);
    }

    @Test
    void getSensorsWithCurrentValue_shouldReturnCorrectResponse() {
        var sensor1Value = testDataGenerator.createSensorValue(1);
        var sensor1 = testDataGenerator.createSensor(1, testStore, List.of(sensor1Value));
        var sensor2 = testDataGenerator.createSensor(2, testStore, List.of());
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
                        .value(null)
                        .build()
        );

        Mockito.when(sensorRepository.findByStoreIdWithLastValue(testStore.getId()))
                .thenReturn(List.of(sensor1, sensor2));

        var response = sensorService.getSensorsWithCurrentValue(testStore.getId());

        Assertions.assertEquals(expectedResponse, response);
    }
}
