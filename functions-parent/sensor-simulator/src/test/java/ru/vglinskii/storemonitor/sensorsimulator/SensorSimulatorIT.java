package ru.vglinskii.storemonitor.sensorsimulator;

import java.util.Objects;
import java.util.stream.Stream;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vglinskii.storemonitor.functionscommon.api.HttpResponse;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonSensorDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonStoreDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.model.Sensor;
import ru.vglinskii.storemonitor.functionscommon.model.Store;
import ru.vglinskii.storemonitor.functionscommon.utils.TestContext;
import ru.vglinskii.storemonitor.sensorsimulator.api.DevicesApi;
import ru.vglinskii.storemonitor.sensorsimulator.config.SensorSimulatorConfig;
import ru.vglinskii.storemonitor.sensorsimulator.dto.UpdateSensorsValuesDtoRequest;

@ExtendWith(MockitoExtension.class)
public class SensorSimulatorIT {
    private Handler handler;
    private DevicesApi devicesApi;

    private CommonStoreDao storeDao;
    private CommonSensorDao sensorDao;

    private Store store1;
    private Sensor sensor1;
    private Sensor sensor2;

    public SensorSimulatorIT() {
        var databaseConnectivity = DatabaseConnectivityFactory.create(
                "jdbc:mysql://localhost:3306/store-monitor-test",
                "root",
                "root"
        );
        this.devicesApi = Mockito.mock(DevicesApi.class);
        this.handler = new Handler(
                databaseConnectivity,
                devicesApi,
                SensorSimulatorConfig.builder()
                        .sensorValueCelsiusMean(-3)
                        .sensorValueCelsiusStandardDeviation(3)
                        .build()
        );
        this.storeDao = new CommonStoreDao(databaseConnectivity);
        this.sensorDao = new CommonSensorDao(databaseConnectivity);
    }

    @BeforeEach
    void init() {
        sensorDao.deleteAll();
        storeDao.deleteAll();

        store1 = storeDao.insert(Store.builder().location("Location 1").build());

        sensor1 = sensorDao.insert(
                Sensor.builder()
                        .storeId(store1.getId())
                        .location("Location 1")
                        .inventoryNumber("00000001")
                        .factoryCode("TS_260420240907")
                        .build());
        sensor2 = sensorDao.insert(
                Sensor.builder()
                        .storeId(store1.getId())
                        .location("Location 2")
                        .inventoryNumber("00000002")
                        .factoryCode("TS_260420240908")
                        .build());
    }

    @Test
    void handle_shouldSendUpdateValuesRequest() {
        var updateRequestCaptor = ArgumentCaptor.forClass(UpdateSensorsValuesDtoRequest.class);
        Mockito.when(devicesApi.updateSensorsValues(updateRequestCaptor.capture()))
                .thenReturn(
                        new HttpResponse(HttpStatus.SC_OK, null)
                );

        triggerSimulation();

        var sentRequest = updateRequestCaptor.getValue();

        Assertions.assertEquals(2, sentRequest.getValues().size());
        Assertions.assertTrue(
                Stream.of(sensor1, sensor2)
                        .allMatch((sensor) -> sentRequest.getValues().stream()
                                .anyMatch((valueUpdateRequest) -> Objects.equals(
                                        valueUpdateRequest.getSensorId(),
                                        sensor.getId()
                                ))
                        )
        );
    }

    private void triggerSimulation() {
        handler.handle(null, new TestContext());
    }
}
