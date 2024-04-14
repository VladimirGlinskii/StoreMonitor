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
import ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport.DecommissionedReportDtoResponse;
import ru.vglinskii.storemonitor.baseapi.model.DecommissionedReport;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.baseapi.repository.DecommissionedReportRepository;
import ru.vglinskii.storemonitor.baseapi.repository.EmployeeRepository;
import ru.vglinskii.storemonitor.baseapi.repository.StoreRepository;
import ru.vglinskii.storemonitor.baseapi.utils.TestDataGenerator;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DecommissionedReportIntegrationTest extends IntegrationTestBase {
    private static final String API_URL_TEMPLATE = UriComponentsBuilder
            .fromPath("/api/decommissioned-reports")
            .queryParam("from", "{from}")
            .queryParam("to", "{to}")
            .encode()
            .toUriString();
    private static final Instant BASE_DATE = Instant.parse("2020-01-01T00:00:00Z");
    private final TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DecommissionedReportRepository decommissionedReportRepository;

    private Store store1;
    private Store store2;
    private Employee directorFromStore1;

    private List<DecommissionedReport> reportsInStore1;
    private List<DecommissionedReport> reportsInStore2;

    @BeforeEach
    public void init() {
        decommissionedReportRepository.deleteAll();
        storeRepository.deleteAll();

        store1 = storeRepository.save(testDataGenerator.createStore(1));
        store2 = storeRepository.save(testDataGenerator.createStore(2));

        directorFromStore1 = employeeRepository.save(
                testDataGenerator.createEmployee(1, store1, EmployeeType.DIRECTOR)
        );

        reportsInStore1 = List.of(
                decommissionedReportRepository.save(
                        testDataGenerator.createDecommissionedReport(
                                1,
                                store1,
                                BASE_DATE
                        )
                ),
                decommissionedReportRepository.save(
                        testDataGenerator.createDecommissionedReport(
                                2,
                                store1,
                                BASE_DATE.plus(1, ChronoUnit.DAYS)
                        )
                ),
                decommissionedReportRepository.save(
                        testDataGenerator.createDecommissionedReport(
                                3,
                                store1,
                                BASE_DATE.plus(2, ChronoUnit.DAYS)
                        )
                ),
                decommissionedReportRepository.save(
                        testDataGenerator.createDecommissionedReport(
                                4,
                                store1,
                                BASE_DATE.plus(3, ChronoUnit.DAYS)
                        )
                )
        );

        reportsInStore2 = List.of(
                decommissionedReportRepository.save(
                        testDataGenerator.createDecommissionedReport(
                                5,
                                store2,
                                BASE_DATE
                        )
                ),
                decommissionedReportRepository.save(
                        testDataGenerator.createDecommissionedReport(
                                6,
                                store2,
                                BASE_DATE.plus(1, ChronoUnit.DAYS)
                        )
                )
        );
    }

    private static Stream<Arguments> getValidRequestsAndExpectedResponsesForGetAll() {
        return Stream.of(
                // All values inside interval
                Arguments.of(
                        BASE_DATE,
                        BASE_DATE.plus(3, ChronoUnit.DAYS),
                        List.of(0, 1, 2, 3)
                ),
                // First part inside interval
                Arguments.of(
                        BASE_DATE,
                        BASE_DATE.plus(1, ChronoUnit.DAYS),
                        List.of(0, 1)
                ),
                // Last part inside interval
                Arguments.of(
                        BASE_DATE.plus(2, ChronoUnit.DAYS),
                        BASE_DATE.plus(3, ChronoUnit.DAYS),
                        List.of(2, 3)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getValidRequestsAndExpectedResponsesForGetAll")
    void whenValid_getAll_shouldReturnCorrectResponse(
            Instant from,
            Instant to,
            List<Integer> expectedReportsIndexes
    ) {
        var expectedResponse = expectedReportsIndexes.stream()
                .map((i) -> reportsInStore1.get(i))
                .map((r) -> DecommissionedReportDtoResponse.builder()
                        .link(r.getLink())
                        .datetime(r.getCreatedAt())
                        .build()
                )
                .toArray();

        var response = restTemplate.exchange(
                API_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(createAuthorizationHeader(directorFromStore1)),
                DecommissionedReportDtoResponse[].class,
                prepareParams(from, to)
        );
        var responseBody = response.getBody();

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertArrayEquals(expectedResponse, responseBody);
    }

    @Test
    void whenUnauthorized_getAll_shouldReturnUnauthorizedCode() {
        var response = restTemplate.exchange(
                API_URL_TEMPLATE,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                DecommissionedReportDtoResponse[].class,
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
