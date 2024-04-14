package ru.vglinskii.storemonitor.baseapi.integration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersWorkSummaryDtoResponse;
import ru.vglinskii.storemonitor.baseapi.model.CashRegister;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.baseapi.repository.CashRegisterRepository;
import ru.vglinskii.storemonitor.baseapi.repository.CashRegisterSessionRepository;
import ru.vglinskii.storemonitor.baseapi.repository.EmployeeRepository;
import ru.vglinskii.storemonitor.baseapi.repository.StoreRepository;
import ru.vglinskii.storemonitor.baseapi.utils.TestDataGenerator;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CashRegisterWorkSummaryIntegrationTest extends IntegrationTestBase {
    private static final String API_URL_TEMPLATE = UriComponentsBuilder
            .fromPath("/api/cash-registers/work-summary")
            .queryParam("from", "{from}")
            .queryParam("to", "{to}")
            .encode()
            .toUriString();
    private static final Instant BASE_DATE = Instant.parse("2020-01-01T00:00:00Z");
    private final TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CashRegisterRepository cashRegisterRepository;
    @Autowired
    private CashRegisterSessionRepository cashRegisterSessionRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @MockBean
    private DateTimeProvider dateTimeProvider;
    @SpyBean
    private AuditingHandler handler;

    private Store store1;
    private Store store2;
    private Employee directorFromStore1;
    private Employee cashier1FromStore1;
    private Employee cashier2FromStore1;
    private Employee cashier1FromStore2;

    private CashRegister cashRegister1InStore1;
    private CashRegister cashRegister2InStore1;
    private CashRegister cashRegister1InStore2;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        handler.setDateTimeProvider(dateTimeProvider);

        cashRegisterSessionRepository.deleteAll();
        cashRegisterRepository.deleteAll();
        employeeRepository.deleteAll();
        storeRepository.deleteAll();

        store1 = storeRepository.save(testDataGenerator.createStore(1));
        store2 = storeRepository.save(testDataGenerator.createStore(2));

        directorFromStore1 = employeeRepository.save(
                testDataGenerator.createEmployee(1, store1, EmployeeType.DIRECTOR)
        );

        cashier1FromStore1 = employeeRepository.save(
                testDataGenerator.createEmployee(101, store1, EmployeeType.CASHIER)
        );
        cashier2FromStore1 = employeeRepository.save(
                testDataGenerator.createEmployee(102, store1, EmployeeType.CASHIER)
        );
        cashier1FromStore2 = employeeRepository.save(
                testDataGenerator.createEmployee(103, store2, EmployeeType.CASHIER)
        );

        cashRegister1InStore1 = cashRegisterRepository.save(testDataGenerator.createCashRegister(1, store1));
        cashRegister2InStore1 = cashRegisterRepository.save(testDataGenerator.createCashRegister(2, store1));
        cashRegister1InStore2 = cashRegisterRepository.save(testDataGenerator.createCashRegister(3, store2));

        // Store 1 cash register sessions
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plus(8, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                1,
                cashRegister1InStore1,
                cashier1FromStore1,
                BASE_DATE.plus(10, ChronoUnit.HOURS)
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plus(8, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                2L,
                cashRegister2InStore1,
                cashier2FromStore1,
                BASE_DATE.plus(12, ChronoUnit.HOURS)
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plus(12, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                3L,
                cashRegister1InStore1,
                cashier1FromStore1,
                null
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plus(16, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                4L,
                cashRegister2InStore1,
                cashier2FromStore1,
                BASE_DATE.plus(18, ChronoUnit.HOURS)
        ));

        // Store 2 cash register sessions
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plus(8, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                5L,
                cashRegister1InStore2,
                cashier1FromStore2,
                BASE_DATE.plus(10, ChronoUnit.HOURS)
        ));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                6L,
                cashRegister1InStore2,
                cashier1FromStore2,
                BASE_DATE.plus(12, ChronoUnit.HOURS)
        ));
    }

    private static Stream<Arguments> getValidRequestsAndExpectedResponsesForGetWorkSummary() {
        return Stream.of(
                // All sessions inside interval
                Arguments.of(
                        BASE_DATE,
                        BASE_DATE.plus(1, ChronoUnit.DAYS),
                        "0d 20h 0m 0s"
                ),
                // All sessions started before interval but one still opened
                Arguments.of(
                        BASE_DATE.plus(1, ChronoUnit.DAYS),
                        BASE_DATE.plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS),
                        "0d 2h 0m 0s"
                ),
                // All sessions started after interval
                Arguments.of(
                        BASE_DATE.minus(1, ChronoUnit.DAYS),
                        BASE_DATE,
                        "0d 0h 0m 0s"
                ),
                // Some sessions inside interval
                Arguments.of(
                        BASE_DATE.minus(1, ChronoUnit.DAYS),
                        BASE_DATE.plus(12, ChronoUnit.HOURS),
                        "0d 6h 0m 0s"
                ),
                // 2 sessions outside interval
                Arguments.of(
                        BASE_DATE.plus(8, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES),
                        BASE_DATE.plus(9, ChronoUnit.HOURS).plus(20, ChronoUnit.MINUTES),
                        "0d 1h 40m 0s"
                ),
                // Some sessions started outside interval and ended inside
                Arguments.of(
                        BASE_DATE.plus(8, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES),
                        BASE_DATE.plus(11, ChronoUnit.HOURS),
                        "0d 4h 0m 0s"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getValidRequestsAndExpectedResponsesForGetWorkSummary")
    void whenValid_getWorkSummary_shouldReturnCorrectResponse(
            Instant from,
            Instant to,
            String expectedDuration
    ) {
        var response = restTemplate.exchange(
                API_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(createAuthorizationHeader(directorFromStore1)),
                CashRegistersWorkSummaryDtoResponse.class,
                prepareParams(from, to)
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(expectedDuration, response.getBody().getDuration());
    }

    @Test
    void whenUnauthorized_getWorkSummary_shouldReturnUnauthorizedCode() {
        var response = restTemplate.exchange(
                API_URL_TEMPLATE,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                CashRegistersWorkSummaryDtoResponse.class,
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
