package ru.vglinskii.storemonitor.baseapi.integration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import ru.vglinskii.storemonitor.baseapi.dto.ErrorDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.ErrorsDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterStatusDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.exception.ErrorCode;
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
public class CashRegisterIT extends IntegrationTestBase {
    private final String BASE_API_URL = "/api/cash-registers";
    private final TestDataGenerator testDataGenerator = new TestDataGenerator();
    private static final Instant BASE_DATE = Instant.parse("2020-01-01T00:00:00Z");

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
    private Employee directorFromStore2;
    private Employee cashier1FromStore1;
    private Employee cashier2FromStore1;
    private Employee cashier1FromStore2;

    private CashRegister cashRegister1InStore1;
    private CashRegister cashRegister2InStore1;
    private CashRegister cashRegister1InStore2;
    private CashRegister cashRegister2InStore2;

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
        directorFromStore2 = employeeRepository.save(
                testDataGenerator.createEmployee(2, store2, EmployeeType.DIRECTOR)
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
        cashRegister2InStore2 = cashRegisterRepository.save(testDataGenerator.createCashRegister(4, store2));

        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                1,
                cashRegister1InStore1,
                cashier1FromStore1,
                BASE_DATE.plus(1, ChronoUnit.HOURS)
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plus(1, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                2,
                cashRegister1InStore1,
                cashier2FromStore1,
                BASE_DATE.plus(2, ChronoUnit.HOURS)
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.plus(2, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                3,
                cashRegister1InStore1,
                cashier2FromStore1,
                null
        ));

        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.minus(10, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                4,
                cashRegister1InStore2,
                cashier1FromStore2,
                BASE_DATE.minus(9, ChronoUnit.HOURS)
        ));
        Mockito.when(dateTimeProvider.getNow()).thenReturn(Optional.of(BASE_DATE.minus(9, ChronoUnit.HOURS)));
        cashRegisterSessionRepository.save(testDataGenerator.createCashRegisterSession(
                5,
                cashRegister1InStore2,
                cashier1FromStore2,
                BASE_DATE.minus(8, ChronoUnit.HOURS)
        ));
    }

    @Test
    void whenUnauthorized_create_shouldReturnUnauthorizedCode() {
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber("00000020")
                .build();

        var response = restTemplate.postForEntity(
                BASE_API_URL,
                new HttpEntity<>(request),
                null
        );
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void whenValid_create_shouldReturnSuccessResponse() {
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber("00000020")
                .build();

        var response = restTemplate.postForEntity(
                BASE_API_URL,
                new HttpEntity<>(request, createAuthorizationHeader(directorFromStore1)),
                CashRegisterDtoResponse.class
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        var responseBody = response.getBody();

        Assertions.assertEquals(request.getInventoryNumber(), responseBody.getInventoryNumber());
        Assertions.assertNotNull(responseBody.getCreatedAt());
        Assertions.assertNotNull(responseBody.getUpdatedAt());
        Assertions.assertTrue(responseBody.getId() > 0);
    }

    @Test
    void whenCashRegisterExists_create_shouldReturnError() {
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber(cashRegister1InStore1.getInventoryNumber())
                .build();
        var expectedError = new ErrorDtoResponse(ErrorCode.CASH_REGISTER_EXISTS);

        var response = restTemplate.postForEntity(
                BASE_API_URL,
                new HttpEntity<>(request, createAuthorizationHeader(directorFromStore1)),
                ErrorsDtoResponse.class
        );
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        var responseBody = response.getBody();

        Assertions.assertEquals(response.getStatusCode().value(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(1, responseBody.getErrors().size());
        Assertions.assertEquals(expectedError, responseBody.getErrors().get(0));
    }

    @Test
    void whenCashRegisterExistsInOtherStore_create_shouldReturnSuccessResponse() {
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber(cashRegister1InStore2.getInventoryNumber())
                .build();

        var response = restTemplate.postForEntity(
                BASE_API_URL,
                new HttpEntity<>(request, createAuthorizationHeader(directorFromStore1)),
                CashRegisterDtoResponse.class
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        var responseBody = response.getBody();

        Assertions.assertEquals(request.getInventoryNumber(), responseBody.getInventoryNumber());
        Assertions.assertNotNull(responseBody.getCreatedAt());
        Assertions.assertNotNull(responseBody.getUpdatedAt());
        Assertions.assertTrue(responseBody.getId() > 0);
    }

    @Test
    void whenUnauthorized_delete_shouldReturnUnauthorizedCode() {
        var response = restTemplate.exchange(
                BASE_API_URL + "/" + cashRegister1InStore1.getId(),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class
        );
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void whenExistInStore_delete_shouldDelete() {
        var response = restTemplate.exchange(
                BASE_API_URL + "/" + cashRegister1InStore1.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthorizationHeader(directorFromStore1)),
                Void.class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertFalse(cashRegisterRepository.existsById(cashRegister1InStore1.getId()));
    }

    @Test
    void whenExistInOtherStore_delete_shouldNotDelete() {
        var response = restTemplate.exchange(
                BASE_API_URL + "/" + cashRegister1InStore1.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthorizationHeader(directorFromStore2)),
                Void.class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertTrue(cashRegisterRepository.existsById(cashRegister1InStore1.getId()));
    }

    @Test
    void whenUnauthorized_openSession_shouldReturnUnauthorizedCode() {
        var response = restTemplate.postForEntity(
                BASE_API_URL + "/" + cashRegister1InStore2.getId() + "/sessions",
                HttpEntity.EMPTY,
                Void.class
        );
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void whenCashRegisterNotExistInStore_openSession_shouldReturnError() {
        var response = restTemplate.postForEntity(
                BASE_API_URL + "/" + cashRegister1InStore2.getId() + "/sessions",
                new HttpEntity<>(createAuthorizationHeader(cashier1FromStore1)),
                ErrorsDtoResponse.class
        );
        var responseBody = response.getBody();

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals(1, responseBody.getErrors().size());
        Assertions.assertEquals(ErrorCode.CASH_REGISTER_NOT_FOUND, responseBody.getErrors().get(0).getErrorCode());
    }

    @Test
    void whenDirector_openSession_shouldReturnError() {
        var response = restTemplate.postForEntity(
                BASE_API_URL + "/" + cashRegister1InStore1.getId() + "/sessions",
                new HttpEntity<>(createAuthorizationHeader(directorFromStore1)),
                ErrorsDtoResponse.class
        );
        var responseBody = response.getBody();

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals(1, responseBody.getErrors().size());
        Assertions.assertEquals(ErrorCode.CASHIER_NOT_FOUND, responseBody.getErrors().get(0).getErrorCode());
    }

    @Test
    void whenClosed_openSession_shouldOpenNewSession() {
        var response = restTemplate.postForEntity(
                BASE_API_URL + "/" + cashRegister1InStore2.getId() + "/sessions",
                new HttpEntity<>(createAuthorizationHeader(cashier1FromStore2)),
                Void.class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        var createdSession = cashRegisterSessionRepository
                .findActiveByCashRegisterId(cashRegister1InStore2.getId())
                .get();
        Assertions.assertNull(createdSession.getClosedAt());
        Assertions.assertEquals(cashRegister1InStore2.getId(), createdSession.getCashRegister().getId());
        Assertions.assertEquals(cashier1FromStore2.getId(), createdSession.getCashier().getId());
    }

    @Test
    void whenOpened_openSession_shouldCloseSessionAndOpenNewSession() {
        var previousSession = cashRegisterSessionRepository
                .findActiveByCashRegisterId(cashRegister1InStore1.getId())
                .get();
        Assertions.assertNull(previousSession.getClosedAt());

        var response = restTemplate.postForEntity(
                BASE_API_URL + "/" + cashRegister1InStore1.getId() + "/sessions",
                new HttpEntity<>(createAuthorizationHeader(cashier1FromStore1)),
                Void.class
        );
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        previousSession = cashRegisterSessionRepository.findById(previousSession.getId())
                .get();
        Assertions.assertNotNull(previousSession.getClosedAt());

        var createdSession = cashRegisterSessionRepository
                .findActiveByCashRegisterId(cashRegister1InStore1.getId())
                .get();
        Assertions.assertNull(createdSession.getClosedAt());
        Assertions.assertEquals(cashRegister1InStore1.getId(), createdSession.getCashRegister().getId());
        Assertions.assertEquals(cashier1FromStore1.getId(), createdSession.getCashier().getId());
    }

    @Test
    void whenUnauthorized_closeSession_shouldReturnUnauthorizedCode() {
        var response = restTemplate.exchange(
                BASE_API_URL + "/" + cashRegister1InStore1.getId() + "/sessions",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class
        );
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void whenCashRegisterNotExistInStore_closeSession_shouldReturnError() {
        var response = restTemplate.exchange(
                BASE_API_URL + "/" + cashRegister1InStore2.getId() + "/sessions",
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthorizationHeader(cashier1FromStore1)),
                ErrorsDtoResponse.class
        );
        var responseBody = response.getBody();

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals(1, responseBody.getErrors().size());
        Assertions.assertEquals(ErrorCode.CASH_REGISTER_NOT_FOUND, responseBody.getErrors().get(0).getErrorCode());
    }

    @Test
    void whenCashRegisterClosed_closeSession_shouldReturnError() {
        var response = restTemplate.exchange(
                BASE_API_URL + "/" + cashRegister1InStore2.getId() + "/sessions",
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthorizationHeader(cashier1FromStore2)),
                ErrorsDtoResponse.class
        );
        var responseBody = response.getBody();

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals(1, responseBody.getErrors().size());
        Assertions.assertEquals(ErrorCode.CASH_REGISTER_ALREADY_CLOSED, responseBody.getErrors().get(0).getErrorCode());
    }

    @Test
    void whenCashRegisterOpenedByOther_closeSession_shouldReturnError() {
        var response = restTemplate.exchange(
                BASE_API_URL + "/" + cashRegister1InStore1.getId() + "/sessions",
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthorizationHeader(cashier1FromStore1)),
                ErrorsDtoResponse.class
        );
        var responseBody = response.getBody();

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals(1, responseBody.getErrors().size());
        Assertions.assertEquals(ErrorCode.CASH_REGISTER_OPENED_BY_OTHER, responseBody.getErrors().get(0).getErrorCode());
    }

    @Test
    void whenCashRegisterOpenedSameCashier_closeSession_shouldCloseSession() {
        var lastSession = cashRegisterSessionRepository
                .findActiveByCashRegisterId(cashRegister1InStore1.getId())
                .get();
        Assertions.assertNull(lastSession.getClosedAt());

        var response = restTemplate.exchange(
                BASE_API_URL + "/" + cashRegister1InStore1.getId() + "/sessions",
                HttpMethod.DELETE,
                new HttpEntity<>(createAuthorizationHeader(cashier2FromStore1)),
                Void.class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        lastSession = cashRegisterSessionRepository.findById(lastSession.getId())
                .get();
        Assertions.assertNotNull(lastSession.getClosedAt());
    }

    @Test
    void whenUnauthorized_getStatuses_shouldReturnUnauthorizedCode() {
        var response = restTemplate.exchange(
                BASE_API_URL + "/statuses",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                CashRegisterStatusDtoResponse[].class
        );
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void whenStore1_getStatuses_shouldReturnCorrectResponse() {
        var expectedResponse = new CashRegisterStatusDtoResponse[]{
                CashRegisterStatusDtoResponse.builder()
                        .id(cashRegister1InStore1.getId())
                        .inventoryNumber(cashRegister1InStore1.getInventoryNumber())
                        .opened(true)
                        .build(),
                CashRegisterStatusDtoResponse.builder()
                        .id(cashRegister2InStore1.getId())
                        .inventoryNumber(cashRegister2InStore1.getInventoryNumber())
                        .opened(false)
                        .build()
        };

        var response = restTemplate.exchange(
                BASE_API_URL + "/statuses",
                HttpMethod.GET,
                new HttpEntity<>(createAuthorizationHeader(directorFromStore1)),
                CashRegisterStatusDtoResponse[].class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertArrayEquals(expectedResponse, response.getBody());
    }

    @Test
    void whenStore2_getStatuses_shouldReturnCorrectResponse() {
        var expectedResponse = new CashRegisterStatusDtoResponse[]{
                CashRegisterStatusDtoResponse.builder()
                        .id(cashRegister1InStore2.getId())
                        .inventoryNumber(cashRegister1InStore2.getInventoryNumber())
                        .opened(false)
                        .build(),
                CashRegisterStatusDtoResponse.builder()
                        .id(cashRegister2InStore2.getId())
                        .inventoryNumber(cashRegister2InStore2.getInventoryNumber())
                        .opened(false)
                        .build()
        };

        var response = restTemplate.exchange(
                BASE_API_URL + "/statuses",
                HttpMethod.GET,
                new HttpEntity<>(createAuthorizationHeader(directorFromStore2)),
                CashRegisterStatusDtoResponse[].class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertArrayEquals(expectedResponse, response.getBody());
    }
}
