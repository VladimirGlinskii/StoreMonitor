package ru.vglinskii.storemonitor.incidentsreport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;
import ru.vglinskii.storemonitor.common.enums.IncidentType;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonEmployeeDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonIncidentDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonStoreDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpResponseDto;
import ru.vglinskii.storemonitor.functionscommon.dto.RequestContext;
import ru.vglinskii.storemonitor.functionscommon.model.Employee;
import ru.vglinskii.storemonitor.functionscommon.model.Incident;
import ru.vglinskii.storemonitor.functionscommon.model.Store;
import ru.vglinskii.storemonitor.functionscommon.utils.TestContext;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import ru.vglinskii.storemonitor.incidentsreport.dto.IncidentsReportDtoResponse;

public class IncidentsReportIT {
    private static final Instant BASE_DATE = Instant.parse("2020-01-01T00:00:00Z");
    private Handler handler;
    private ObjectMapper objectMapper;
    private CommonStoreDao storeDao;
    private CommonEmployeeDao employeeDao;
    private CommonIncidentDao incidentDao;
    private Store store1;
    private Employee directorInStore1;
    private Store store2;
    private Employee directorInStore2;

    public IncidentsReportIT() {
        var databaseConnectivity = DatabaseConnectivityFactory.create(
                "jdbc:mysql://localhost:3306/store-monitor-test",
                "root",
                "root"
        );
        this.handler = new Handler(databaseConnectivity);
        this.objectMapper = new AppObjectMapper();
        this.storeDao = new CommonStoreDao(databaseConnectivity);
        this.employeeDao = new CommonEmployeeDao(databaseConnectivity);
        this.incidentDao = new CommonIncidentDao(databaseConnectivity);
    }

    @BeforeEach
    void init() {
        incidentDao.deleteAll();
        employeeDao.deleteAll();
        storeDao.deleteAll();

        store1 = storeDao.insert(
                Store.builder()
                        .location("Location 1")
                        .build()
        );
        store2 = storeDao.insert(
                Store.builder()
                        .location("Location 2")
                        .build()
        );

        directorInStore1 = employeeDao.insert(
                Employee.builder()
                        .firstName("Firstname1")
                        .lastName("Lastname1")
                        .type(EmployeeType.DIRECTOR)
                        .storeId(store1.getId())
                        .secret("secret1")
                        .build()
        );
        directorInStore1 = employeeDao.insert(
                Employee.builder()
                        .firstName("Firstname2")
                        .lastName("Lastname2")
                        .type(EmployeeType.DIRECTOR)
                        .storeId(store2.getId())
                        .secret("secret2")
                        .build()
        );

        // Incidents in store 1
        incidentDao.create(
                Incident.builder()
                        .storeId(store1.getId())
                        .eventType(IncidentType.INJURY)
                        .datetime(BASE_DATE.plus(8, ChronoUnit.HOURS))
                        .description("Description 1")
                        .calledAmbulance(true)
                        .calledFireDepartment(true)
                        .calledGasService(false)
                        .calledPolice(false)
                        .build()
        );
        incidentDao.create(
                Incident.builder()
                        .storeId(store1.getId())
                        .eventType(IncidentType.HOLIGANITY)
                        .datetime(BASE_DATE.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.HOURS))
                        .description("Description 2")
                        .calledAmbulance(false)
                        .calledFireDepartment(true)
                        .calledGasService(true)
                        .calledPolice(false)
                        .build()
        );
        incidentDao.create(
                Incident.builder()
                        .storeId(store1.getId())
                        .eventType(IncidentType.THEFT)
                        .datetime(BASE_DATE.plus(1, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS))
                        .description("Description 3")
                        .calledAmbulance(false)
                        .calledFireDepartment(false)
                        .calledGasService(true)
                        .calledPolice(true)
                        .build()
        );
        incidentDao.create(
                Incident.builder()
                        .storeId(store1.getId())
                        .eventType(IncidentType.THEFT)
                        .datetime(BASE_DATE.plus(2, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS))
                        .description("Description 4")
                        .calledAmbulance(true)
                        .calledFireDepartment(false)
                        .calledGasService(false)
                        .calledPolice(true)
                        .build()
        );

        // Incidents in store 2
        incidentDao.create(
                Incident.builder()
                        .storeId(store2.getId())
                        .eventType(IncidentType.HOLIGANITY)
                        .datetime(BASE_DATE.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.HOURS))
                        .description("Description 5")
                        .calledAmbulance(false)
                        .calledFireDepartment(true)
                        .calledGasService(true)
                        .calledPolice(false)
                        .build()
        );
        incidentDao.create(
                Incident.builder()
                        .storeId(store2.getId())
                        .eventType(IncidentType.THEFT)
                        .datetime(BASE_DATE.plus(1, ChronoUnit.DAYS).plus(12, ChronoUnit.HOURS))
                        .description("Description 6")
                        .calledAmbulance(false)
                        .calledFireDepartment(false)
                        .calledGasService(true)
                        .calledPolice(true)
                        .build()
        );
    }

    private static Stream<Arguments> getIntervalsWithExpectedReports() {
        return Stream.of(
                // All incidents inside
                Arguments.of(
                        BASE_DATE,
                        BASE_DATE.plus(3, ChronoUnit.DAYS),
                        IncidentsReportDtoResponse.builder()
                                .totalIncidents(4)
                                .calledAmbulance(2)
                                .calledFireDepartment(2)
                                .calledGasService(2)
                                .calledPolice(2)
                                .build()
                ),
                // Two incidents inside
                Arguments.of(
                        BASE_DATE.plus(1, ChronoUnit.DAYS),
                        BASE_DATE.plus(2, ChronoUnit.DAYS),
                        IncidentsReportDtoResponse.builder()
                                .totalIncidents(2)
                                .calledAmbulance(0)
                                .calledFireDepartment(1)
                                .calledGasService(2)
                                .calledPolice(1)
                                .build()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getIntervalsWithExpectedReports")
    void whenValid_handle_shouldReturnCorrectReport(
            Instant from,
            Instant to,
            IncidentsReportDtoResponse expectedResponseBody
    ) throws JsonProcessingException {
        var response = sendRequest(
                from.toString(),
                to.toString(),
                new AuthorizationContextDto(store1.getId(), directorInStore1.getId())
        );
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        var responseBody = objectMapper.readValue(response.getBody(), IncidentsReportDtoResponse.class);

        Assertions.assertEquals(expectedResponseBody, responseBody);
    }

    private HttpResponseDto sendRequest(
            String from,
            String to,
            AuthorizationContextDto authorizationContext
    ) {
        var request = HttpRequestDto.builder()
                .queryStringParameters(Map.of(
                        "from", from,
                        "to", to
                ))
                .requestContext(new RequestContext(authorizationContext))
                .build();

        return handler.handle(request, new TestContext());
    }
}
