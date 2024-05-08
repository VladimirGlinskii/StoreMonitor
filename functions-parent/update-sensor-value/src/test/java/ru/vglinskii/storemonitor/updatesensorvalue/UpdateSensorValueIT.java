package ru.vglinskii.storemonitor.updatesensorvalue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Stream;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonSensorDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonStoreDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpResponseDto;
import ru.vglinskii.storemonitor.functionscommon.model.Sensor;
import ru.vglinskii.storemonitor.functionscommon.model.Store;
import ru.vglinskii.storemonitor.functionscommon.utils.TestContext;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.SensorsValuesDtoResponse;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.UpdateSensorValueDtoRequest;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.UpdateSensorsValuesDtoRequest;

public class UpdateSensorValueIT {
    private Handler handler;
    private ObjectMapper objectMapper;
    private CommonStoreDao storeDao;
    private CommonSensorDao sensorDao;
    private Store store1;
    private Sensor sensor1;
    private Sensor sensor2;

    public UpdateSensorValueIT() {
        var databaseConnectivity = DatabaseConnectivityFactory.create(
                "jdbc:mysql://localhost:3306/store-monitor-test",
                "root",
                "root"
        );
        this.handler = new Handler(databaseConnectivity);
        this.objectMapper = new AppObjectMapper();
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
    void whenValid_handle_shouldInsertNewValues() throws Exception {
        var requestBody = UpdateSensorsValuesDtoRequest.builder()
                .values(
                        Stream.of(sensor1, sensor2)
                                .map((sensor) -> UpdateSensorValueDtoRequest.builder()
                                        .value((float) (Math.random() * 10 - 10))
                                        .unit(SensorUnit.CELSIUS)
                                        .sensorId(sensor.getId())
                                        .build()
                                )
                                .toList())
                .build();

        var response = sendRequest(objectMapper.writeValueAsString(requestBody));
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        var responseBody = objectMapper.readValue(response.getBody(), SensorsValuesDtoResponse.class);

        Assertions.assertEquals(2, responseBody.getValues().size());
        Assertions.assertTrue(responseBody.getValues().stream().allMatch((v) -> v.getId() > 0));
    }

    private static Stream<Arguments> getInvalidUpdateValueRequests() {
        return Stream.of(
                Arguments.of(
                        UpdateSensorValueDtoRequest.builder()
                                .value((float) (Math.random() * 10 - 10))
                                .unit(SensorUnit.CELSIUS)
                                .build()
                ),
                Arguments.of(
                        UpdateSensorValueDtoRequest.builder()
                                .value((float) (Math.random() * 10 - 10))
                                .sensorId(1L).build()
                ),
                Arguments.of(
                        UpdateSensorValueDtoRequest.builder()
                                .unit(SensorUnit.CELSIUS)
                                .sensorId(1L)
                                .build()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidUpdateValueRequests")
    void whenInvalid_handle_shouldSkipInvalidValues(UpdateSensorValueDtoRequest dtoRequest) throws Exception {
        var requestBody = UpdateSensorsValuesDtoRequest.builder().values(List.of(dtoRequest)).build();

        var response = sendRequest(objectMapper.writeValueAsString(requestBody));
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        var responseBody = objectMapper.readValue(response.getBody(), SensorsValuesDtoResponse.class);

        Assertions.assertEquals(0, responseBody.getValues().size());
    }

    private HttpResponseDto sendRequest(String requestBody) {
        var request = HttpRequestDto.builder().body(requestBody).build();

        return handler.handle(request, new TestContext());
    }
}
