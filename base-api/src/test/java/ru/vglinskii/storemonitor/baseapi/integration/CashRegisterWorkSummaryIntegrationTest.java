package ru.vglinskii.storemonitor.baseapi.integration;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;
import ru.vglinskii.storemonitor.baseapi.TestBase;
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
public class CashRegisterWorkSummaryIntegrationTest extends TestBase {
    private static final String API_URL = "/api/cash-registers/work-summary";
    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0);
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

        cashier1FromStore1 = employeeRepository.save(
                testDataGenerator.createEmployee(1, store1, EmployeeType.CASHIER)
        );
        cashier2FromStore1 = employeeRepository.save(
                testDataGenerator.createEmployee(2, store1, EmployeeType.CASHIER)
        );
        cashier1FromStore2 = employeeRepository.save(
                testDataGenerator.createEmployee(3, store2, EmployeeType.CASHIER)
        );

        cashRegister1InStore1 = cashRegisterRepository.save(testDataGenerator.createCashRegister(1, store1));
        cashRegister2InStore1 = cashRegisterRepository.save(testDataGenerator.createCashRegister(2, store1));
        cashRegister1InStore2 = cashRegisterRepository.save(testDataGenerator.createCashRegister(3, store2));

        // Store 1 cash register sessions
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plusHours(8)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                1,
                cashRegister1InStore1,
                cashier1FromStore1,
                BASE_DATE.plusHours(10)
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plusHours(8)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                2L,
                cashRegister2InStore1,
                cashier2FromStore1,
                BASE_DATE.plusHours(12)
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plusHours(12)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                3L,
                cashRegister1InStore1,
                cashier1FromStore1,
                null
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plusHours(16)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                4L,
                cashRegister2InStore1,
                cashier2FromStore1,
                BASE_DATE.plusHours(18)
        ));

        // Store 2 cash register sessions
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plusHours(8)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                5L,
                cashRegister1InStore2,
                cashier1FromStore2,
                BASE_DATE.plusHours(10)
        ));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                6L,
                cashRegister1InStore2,
                cashier1FromStore2,
                BASE_DATE.plusHours(12)
        ));
    }

    protected HttpHeaders createHeadersForStore(Store store) {
        var headers = new HttpHeaders();
        headers.set("X-Store-Id", store.getId().toString());

        return headers;
    }

    private static Stream<Arguments> getValidRequestsAndExpectedResponsesForGetWorkSummary() {
        return Stream.of(
                // All sessions inside interval
                Arguments.of(
                        BASE_DATE,
                        BASE_DATE.plusDays(1),
                        "0d 20h 0m 0s"
                ),
                // All sessions started before interval but one still opened
                Arguments.of(
                        BASE_DATE.plusDays(1),
                        BASE_DATE.plusDays(1).plusHours(2),
                        "0d 2h 0m 0s"
                ),
                // All sessions started after interval
                Arguments.of(
                        BASE_DATE.minusDays(1),
                        BASE_DATE,
                        "0d 0h 0m 0s"
                ),
                // Some sessions inside interval
                Arguments.of(
                        BASE_DATE.minusDays(1),
                        BASE_DATE.plusHours(12),
                        "0d 6h 0m 0s"
                ),
                // 2 sessions outside interval
                Arguments.of(
                        BASE_DATE.plusHours(8).plusMinutes(30),
                        BASE_DATE.plusHours(9).plusMinutes(20),
                        "0d 1h 40m 0s"
                ),
                // Some sessions started outside interval and ended inside
                Arguments.of(
                        BASE_DATE.plusHours(8).plusMinutes(30),
                        BASE_DATE.plusHours(11),
                        "0d 4h 0m 0s"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getValidRequestsAndExpectedResponsesForGetWorkSummary")
    void whenValid_getWorkSummary_shouldReturnCorrectResponse(
            LocalDateTime from,
            LocalDateTime to,
            String expectedDuration
    ) {
        Map<String, String> params = new HashMap<>();
        params.put("from", from.toString());
        params.put("to", to.toString());

        String urlTemplate = UriComponentsBuilder.fromPath(API_URL)
                .queryParam("from", "{from}")
                .queryParam("to", "{to}")
                .encode()
                .toUriString();

        var response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                new HttpEntity<>(createHeadersForStore(store1)),
                CashRegistersWorkSummaryDtoResponse.class,
                params
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(expectedDuration, response.getBody().getDuration());
    }
}
