package ru.vglinskii.storemonitor.baseapi.integration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValuesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorsWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorsWithValuesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.model.CashRegisterSession;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.baseapi.model.Sensor;
import ru.vglinskii.storemonitor.baseapi.model.SensorValue;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.baseapi.repository.EmployeeRepository;
import ru.vglinskii.storemonitor.baseapi.repository.SensorRepository;
import ru.vglinskii.storemonitor.baseapi.repository.SensorValueRepository;
import ru.vglinskii.storemonitor.baseapi.repository.StoreRepository;
import ru.vglinskii.storemonitor.baseapi.utils.TestDataGenerator;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SensorIT extends IntegrationTestBase {
    private final String BASE_API_URL = "/api/sensors";
    private static final Instant BASE_DATE = Instant.parse("2020-01-01T00:00:00Z");
    private final TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SensorRepository sensorRepository;
    @Autowired
    private SensorValueRepository sensorValueRepository;

    private Store store1;
    private Store store2;
    private Employee directorFromStore1;

    private Sensor sensor1InStore1;
    private List<SensorValue> sensor1InStore1Values;
    private Sensor sensor2InStore1;
    private List<SensorValue> sensor2InStore1Values;
    private Sensor sensor1InStore2;
    private List<CashRegisterSession> sensor1InStore2Values;

    @BeforeEach
    public void init() {
        sensorValueRepository.deleteAll();
        sensorRepository.deleteAll();
        storeRepository.deleteAll();

        store1 = storeRepository.save(testDataGenerator.createStore(1));
        store2 = storeRepository.save(testDataGenerator.createStore(2));

        directorFromStore1 = employeeRepository.save(
                testDataGenerator.createEmployee(1, store1, EmployeeType.DIRECTOR)
        );

        sensor1InStore1 = sensorRepository.save(testDataGenerator.createSensor(1, store1));
        sensor2InStore1 = sensorRepository.save(testDataGenerator.createSensor(2, store1));
        sensor1InStore2 = sensorRepository.save(testDataGenerator.createSensor(3, store2));

        sensor1InStore1Values = List.of(
                sensorValueRepository.save(testDataGenerator.createSensorValue(1, sensor1InStore1, BASE_DATE)),
                sensorValueRepository.save(testDataGenerator.createSensorValue(
                        2,
                        sensor1InStore1,
                        BASE_DATE.plus(10, ChronoUnit.MINUTES)
                )),
                sensorValueRepository.save(testDataGenerator.createSensorValue(
                        3,
                        sensor1InStore1,
                        BASE_DATE.plus(20, ChronoUnit.MINUTES)
                )),
                sensorValueRepository.save(testDataGenerator.createSensorValue(
                        4,
                        sensor1InStore1,
                        BASE_DATE.plus(30, ChronoUnit.MINUTES)
                ))
        );

        sensor2InStore1Values = List.of(
                sensorValueRepository.save(testDataGenerator.createSensorValue(5, sensor2InStore1, BASE_DATE)),
                sensorValueRepository.save(testDataGenerator.createSensorValue(
                        6,
                        sensor2InStore1,
                        BASE_DATE.plus(10, ChronoUnit.MINUTES)
                )),
                sensorValueRepository.save(testDataGenerator.createSensorValue(
                        7,
                        sensor2InStore1,
                        BASE_DATE.plus(20, ChronoUnit.MINUTES)
                )),
                sensorValueRepository.save(testDataGenerator.createSensorValue(
                        8,
                        sensor2InStore1,
                        BASE_DATE.plus(30, ChronoUnit.MINUTES)
                ))
        );
    }

    @Test
    void whenStore1_getSensors_shouldReturnCorrectResponse() {
        var expectedResponse = new SensorsWithValueDtoResponse(
                List.of(
                        SensorWithValueDtoResponse.builder()
                                .id(sensor1InStore1.getId())
                                .inventoryNumber(sensor1InStore1.getInventoryNumber())
                                .factoryCode(sensor1InStore1.getFactoryCode())
                                .location(sensor1InStore1.getLocation())
                                .value(SensorValueDtoResponse.builder()
                                        .unit(sensor1InStore1Values.getLast().getUnit())
                                        .value(sensor1InStore1Values.getLast().getValue())
                                        .datetime(sensor1InStore1Values.getLast().getDatetime())
                                        .build()
                                )
                                .build(),
                        SensorWithValueDtoResponse.builder()
                                .id(sensor2InStore1.getId())
                                .inventoryNumber(sensor2InStore1.getInventoryNumber())
                                .factoryCode(sensor2InStore1.getFactoryCode())
                                .location(sensor2InStore1.getLocation())
                                .value(SensorValueDtoResponse.builder()
                                        .unit(sensor2InStore1Values.getLast().getUnit())
                                        .value(sensor2InStore1Values.getLast().getValue())
                                        .datetime(sensor2InStore1Values.getLast().getDatetime())
                                        .build()
                                )
                                .build()
                )
        );

        var response = restTemplate.exchange(
                BASE_API_URL,
                HttpMethod.GET,
                new HttpEntity<>(createAuthorizationHeader(directorFromStore1)),
                SensorsWithValueDtoResponse.class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void whenUnauthorized_getSensors_shouldReturnUnauthorizedCode() {
        var response = restTemplate.exchange(
                BASE_API_URL,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                SensorsWithValueDtoResponse.class
        );
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private static Stream<Arguments> getValidRequestsAndExpectedResponsesForGetTemperatureReport() {
        return Stream.of(
                // All values inside interval
                Arguments.of(
                        BASE_DATE,
                        BASE_DATE.plus(1, ChronoUnit.DAYS),
                        List.of(
                                List.of(0, 1, 2, 3),
                                List.of(0, 1, 2, 3)
                        )
                ),
                // First part inside interval
                Arguments.of(
                        BASE_DATE,
                        BASE_DATE.plus(10, ChronoUnit.MINUTES),
                        List.of(
                                List.of(0, 1),
                                List.of(0, 1)
                        )
                ),
                // Last part inside interval
                Arguments.of(
                        BASE_DATE.plus(20, ChronoUnit.MINUTES),
                        BASE_DATE.plus(30, ChronoUnit.MINUTES),
                        List.of(
                                List.of(2, 3),
                                List.of(2, 3)
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getValidRequestsAndExpectedResponsesForGetTemperatureReport")
    void whenValid_getTemperatureReport_shouldReturnCorrectResponse(
            Instant from,
            Instant to,
            List<List<Integer>> expectedSensorValueIndexes
    ) {
        var sensorIdToIndexMap = Map.ofEntries(
                Map.entry(sensor1InStore1.getId(), 0),
                Map.entry(sensor2InStore1.getId(), 1)
        );
        var sensorIdToValuesMap = Map.ofEntries(
                Map.entry(sensor1InStore1.getId(), sensor1InStore1Values),
                Map.entry(sensor2InStore1.getId(), sensor2InStore1Values)
        );
        var expectedSensors = Stream.of(sensor1InStore1, sensor2InStore1)
                .map((sensor) -> SensorWithValuesDtoResponse.builder()
                        .id(sensor.getId())
                        .inventoryNumber(sensor.getInventoryNumber())
                        .factoryCode(sensor.getFactoryCode())
                        .location(sensor.getLocation())
                        .values(expectedSensorValueIndexes.get(sensorIdToIndexMap.get(sensor.getId()))
                                .stream()
                                .map((i) -> sensorIdToValuesMap.get(sensor.getId()).get(i))
                                .map((value) -> SensorValueDtoResponse.builder()
                                        .datetime(value.getDatetime())
                                        .value(value.getValue())
                                        .unit(value.getUnit())
                                        .build()
                                )
                                .toList()
                        )
                        .build()
                )
                .toList();

        String urlTemplate = UriComponentsBuilder
                .fromPath(String.format("%s/temperature", BASE_API_URL))
                .queryParam("from", "{from}")
                .queryParam("to", "{to}")
                .encode()
                .toUriString();

        var response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                new HttpEntity<>(createAuthorizationHeader(directorFromStore1)),
                SensorsWithValuesDtoResponse.class,
                prepareParams(from, to)
        );
        var responseBody = response.getBody();

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertTrue(expectedSensors.containsAll(responseBody.getSensors()));
    }

    @Test
    void whenUnauthorized_getWorkSummary_shouldReturnUnauthorizedCode() {
        String urlTemplate = UriComponentsBuilder
                .fromPath(String.format("%s/temperature", BASE_API_URL))
                .queryParam("from", "{from}")
                .queryParam("to", "{to}")
                .encode()
                .toUriString();

        var response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                SensorsWithValuesDtoResponse.class,
                prepareParams(BASE_DATE, BASE_DATE.plus(1, ChronoUnit.DAYS))
        );
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private Map<String, String> prepareParams(Instant from, Instant to) {
        Map<String, String> params = new HashMap<>();
        params.put("from", from.toString());
        params.put("to", to.toString());

        return params;
    }
}
