package ru.vglinskii.storemonitor.baseapi.service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vglinskii.storemonitor.baseapi.TestBase;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterStatusDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.UpdateCashRegisterStatusDtoRequest;
import ru.vglinskii.storemonitor.baseapi.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.baseapi.exception.ErrorCode;
import ru.vglinskii.storemonitor.baseapi.model.CashRegisterSession;
import ru.vglinskii.storemonitor.baseapi.model.Store;
import ru.vglinskii.storemonitor.baseapi.projection.CashRegisterStatusProjection;
import ru.vglinskii.storemonitor.baseapi.projection.CashRegisterStatusProjectionTestImpl;
import ru.vglinskii.storemonitor.baseapi.repository.CashRegisterRepository;
import ru.vglinskii.storemonitor.baseapi.repository.CashRegisterSessionRepository;
import ru.vglinskii.storemonitor.baseapi.repository.EmployeeRepository;
import ru.vglinskii.storemonitor.baseapi.repository.StoreRepository;
import ru.vglinskii.storemonitor.baseapi.utils.TestDataGenerator;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

@ExtendWith(MockitoExtension.class)
public class CashRegisterServiceTest extends TestBase {
    private final TestDataGenerator testDataGenerator;
    private final Store testStore;
    @Mock
    private CashRegisterRepository cashRegisterRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CashRegisterSessionRepository cashRegisterSessionRepository;
    @InjectMocks
    private CashRegisterService cashRegisterService;

    public CashRegisterServiceTest() {
        this.testDataGenerator = new TestDataGenerator();
        this.testStore = testDataGenerator.createStore(1);
    }

    @Test
    void whenNotExists_create_shouldSuccess() {
        var expectedCashRegister = testDataGenerator.createCashRegister(1, testStore);
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber(expectedCashRegister.getInventoryNumber())
                .build();
        var expectedResponse = CashRegisterDtoResponse.builder()
                .id(expectedCashRegister.getId())
                .inventoryNumber(expectedCashRegister.getInventoryNumber())
                .opened(false)
                .createdAt(expectedCashRegister.getCreatedAt())
                .updatedAt(expectedCashRegister.getUpdatedAt())
                .build();

        Mockito.when(storeRepository.findById(testStore.getId()))
                .thenReturn(Optional.of(testStore));
        Mockito.when(cashRegisterRepository.findByStoreIdAndInventoryNumber(
                        testStore.getId(),
                        expectedCashRegister.getInventoryNumber()
                ))
                .thenReturn(Optional.empty());
        Mockito.when(cashRegisterRepository.save(Mockito.any()))
                .thenReturn(expectedCashRegister);

        var response = cashRegisterService.create(testStore.getId(), request);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    void whenExists_create_shouldThrowException() {
        var existingCashRegister = testDataGenerator.createCashRegister(10, testStore);
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber(existingCashRegister.getInventoryNumber())
                .build();

        Mockito.when(storeRepository.findById(testStore.getId()))
                .thenReturn(Optional.of(testStore));
        Mockito.when(cashRegisterRepository.findByStoreIdAndInventoryNumber(
                        testStore.getId(),
                        existingCashRegister.getInventoryNumber()
                ))
                .thenReturn(Optional.of(existingCashRegister));

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.create(testStore.getId(), request)
        );

        Mockito.verify(cashRegisterRepository, Mockito.never()).save(Mockito.any());
        Assertions.assertEquals(ErrorCode.CASH_REGISTER_EXISTS, exception.getErrorCode());
    }

    @Test
    void delete_shouldDeleteInStore() {
        long targetCashRegisterId = 10;

        cashRegisterService.delete(testStore.getId(), targetCashRegisterId);

        Mockito.verify(cashRegisterRepository, Mockito.times(1))
                .deleteByIdAndStoreId(targetCashRegisterId, testStore.getId());
    }

    @Test
    void whenValidAndNoSession_openSession_shouldCreateSession() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();
        var expectedSessionId = 5L;

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(employeeRepository.findByIdAndStoreIdAndType(
                        cashier.getId(),
                        testStore.getId(),
                        EmployeeType.CASHIER
                ))
                .thenReturn(Optional.of(cashier));
        Mockito.when(cashRegisterSessionRepository.findActiveByCashRegisterId(cashRegister.getId()))
                .thenReturn(Optional.empty());
        Mockito.when(cashRegisterSessionRepository.save(Mockito.any()))
                .thenAnswer((i) -> {
                    CashRegisterSession savedSession = (CashRegisterSession) i.getArguments()[0];
                    savedSession.setId(expectedSessionId);
                    return savedSession;
                });

        cashRegisterService.openSession(
                testStore.getId(),
                cashRegister.getId(),
                request
        );

        var sessionCaptor = ArgumentCaptor.forClass(CashRegisterSession.class);
        Mockito.verify(cashRegisterSessionRepository, Mockito.times(1)).save(sessionCaptor.capture());
        var savedSession = sessionCaptor.getValue();

        Assertions.assertEquals(expectedSessionId, savedSession.getId());
        Assertions.assertEquals(cashRegister.getId(), savedSession.getCashRegister().getId());
        Assertions.assertEquals(cashier.getId(), savedSession.getCashier().getId());
        Assertions.assertNull(savedSession.getClosedAt());
    }

    @Test
    void whenValidAndSessionExists_openSession_shouldClosePreviousSessionAndOpenNew() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var expectedSessionId = 5L;
        var existingSession = testDataGenerator.createCashRegisterSession(
                expectedSessionId - 1,
                cashRegister,
                cashier
        );
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(employeeRepository.findByIdAndStoreIdAndType(
                        cashier.getId(),
                        testStore.getId(),
                        EmployeeType.CASHIER
                ))
                .thenReturn(Optional.of(cashier));
        Mockito.when(cashRegisterSessionRepository.findActiveByCashRegisterId(cashRegister.getId()))
                .thenReturn(Optional.of(existingSession));
        Mockito.when(cashRegisterSessionRepository.save(Mockito.any()))
                .thenAnswer((i) -> i.getArguments()[0]);

        cashRegisterService.openSession(
                testStore.getId(),
                cashRegister.getId(),
                request
        );

        var sessionCaptor = ArgumentCaptor.forClass(CashRegisterSession.class);
        Mockito.verify(cashRegisterSessionRepository, Mockito.times(2)).save(sessionCaptor.capture());
        var closedSession = sessionCaptor.getAllValues().get(0);
        var createdSession = sessionCaptor.getAllValues().get(1);

        Assertions.assertEquals(existingSession.getId(), closedSession.getId());
        Assertions.assertNotNull(closedSession.getClosedAt());
        Assertions.assertNull(createdSession.getClosedAt());
    }

    @Test
    void whenCashRegisterNotExists_openSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.openSession(
                        testStore.getId(),
                        cashRegister.getId(),
                        request
                )
        );

        Assertions.assertEquals(ErrorCode.CASH_REGISTER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void whenCashierNotExists_openSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(employeeRepository.findByIdAndStoreIdAndType(
                        cashier.getId(),
                        testStore.getId(),
                        EmployeeType.CASHIER
                ))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.openSession(
                        testStore.getId(),
                        cashRegister.getId(),
                        request
                )
        );

        Assertions.assertEquals(ErrorCode.CASHIER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void whenValidAndSessionExists_closeSession_shouldCloseSession() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var existingSession = testDataGenerator.createCashRegisterSession(
                5,
                cashRegister,
                cashier
        );
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(employeeRepository.findByIdAndStoreIdAndType(
                        cashier.getId(),
                        testStore.getId(),
                        EmployeeType.CASHIER
                ))
                .thenReturn(Optional.of(cashier));
        Mockito.when(cashRegisterSessionRepository.findActiveByCashRegisterId(cashRegister.getId()))
                .thenReturn(Optional.of(existingSession));
        Mockito.when(cashRegisterSessionRepository.save(Mockito.any()))
                .thenAnswer((i) -> i.getArguments()[0]);

        cashRegisterService.closeSession(
                testStore.getId(),
                cashRegister.getId(),
                request
        );

        var sessionCaptor = ArgumentCaptor.forClass(CashRegisterSession.class);
        Mockito.verify(cashRegisterSessionRepository, Mockito.times(1)).save(sessionCaptor.capture());
        var closedSession = sessionCaptor.getValue();

        Assertions.assertEquals(existingSession.getId(), closedSession.getId());
        Assertions.assertNotNull(closedSession.getClosedAt());
    }

    @Test
    void whenSessionNotExists_closeSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(employeeRepository.findByIdAndStoreIdAndType(
                        cashier.getId(),
                        testStore.getId(),
                        EmployeeType.CASHIER
                ))
                .thenReturn(Optional.of(cashier));
        Mockito.when(cashRegisterSessionRepository.findActiveByCashRegisterId(cashRegister.getId()))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.closeSession(
                        testStore.getId(),
                        cashRegister.getId(),
                        request
                )
        );

        Assertions.assertEquals(ErrorCode.CASH_REGISTER_ALREADY_CLOSED, exception.getErrorCode());
    }

    @Test
    void whenCashRegisterNotExists_closeSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.closeSession(
                        testStore.getId(),
                        cashRegister.getId(),
                        request
                )
        );

        Assertions.assertEquals(ErrorCode.CASH_REGISTER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void whenCashierNotExists_closeSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(employeeRepository.findByIdAndStoreIdAndType(
                        cashier.getId(),
                        testStore.getId(),
                        EmployeeType.CASHIER
                ))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.closeSession(
                        testStore.getId(),
                        cashRegister.getId(),
                        request
                )
        );

        Assertions.assertEquals(ErrorCode.CASHIER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void whenSessionOpenedByOther_closeSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        var otherCashier = testDataGenerator.createEmployee(101, testStore, EmployeeType.CASHIER);
        var existingSession = testDataGenerator.createCashRegisterSession(
                5,
                cashRegister,
                otherCashier
        );
        var request = UpdateCashRegisterStatusDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(employeeRepository.findByIdAndStoreIdAndType(
                        cashier.getId(),
                        testStore.getId(),
                        EmployeeType.CASHIER
                ))
                .thenReturn(Optional.of(cashier));
        Mockito.when(cashRegisterSessionRepository.findActiveByCashRegisterId(cashRegister.getId()))
                .thenReturn(Optional.of(existingSession));

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.closeSession(
                        testStore.getId(),
                        cashRegister.getId(),
                        request
                )
        );

        Assertions.assertEquals(ErrorCode.CASH_REGISTER_OPENED_BY_OTHER, exception.getErrorCode());
    }

    @Test
    void getStatuses_shouldReturnCorrectResponse() {
        List<CashRegisterStatusProjection> projections = List.of(
                new CashRegisterStatusProjectionTestImpl(1, "1", LocalDateTime.now(), null),
                new CashRegisterStatusProjectionTestImpl(1, "1", LocalDateTime.now(), LocalDateTime.now()),
                new CashRegisterStatusProjectionTestImpl(1, "1", null, null)
        );
        var expectedResponse = List.of(
                new CashRegisterStatusDtoResponse(1, "1", true),
                new CashRegisterStatusDtoResponse(1, "1", false),
                new CashRegisterStatusDtoResponse(1, "1", false)
        );

        Mockito.when(cashRegisterRepository.findWithLastSessionByStoreId(testStore.getId()))
                .thenReturn(projections);

        var response = cashRegisterService.getStatuses(testStore.getId());

        Assertions.assertEquals(expectedResponse, response);
    }

    private static Stream<Arguments> getSessionsForAtomicDurationCalculationTest() {
        var from = LocalDateTime.of(2020, Month.JANUARY, 5, 16, 0);
        var to = from.plusHours(5);

        return Stream.of(
                // Opened before interval and closed inside
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.minusMinutes(30))
                                .closedAt(from.plusHours(2).plusSeconds(20))
                                .build(),
                        "0d 2h 0m 20s"
                ),
                // Opened before interval and closed after
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.minusMinutes(30))
                                .closedAt(to.plusSeconds(30))
                                .build(),
                        "0d 5h 0m 0s"
                ),
                // Equal to interval
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from)
                                .closedAt(to)
                                .build(),
                        "0d 5h 0m 0s"
                ),
                // Opened at interval start and closed inside +1 hrs
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from)
                                .closedAt(from.plusHours(1).plusMinutes(30).plusSeconds(40))
                                .build(),
                        "0d 1h 30m 40s"
                ),
                // Opened inside interval and closed after
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.plusHours(4).plusMinutes(30))
                                .closedAt(from.plusHours(5).plusMinutes(30))
                                .build(),
                        "0d 0h 30m 0s"
                ),
                // Opened inside interval and closed inside
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.plusHours(3).plusMinutes(30))
                                .closedAt(from.plusHours(4).plusMinutes(10))
                                .build(),
                        "0d 0h 40m 0s"
                ),
                // Opened before interval and not closed
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.minusMinutes(40))
                                .closedAt(null)
                                .build(),
                        "0d 5h 0m 0s"
                ),
                // Opened inside interval and not closed
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.plusMinutes(40).plusSeconds(40))
                                .closedAt(null)
                                .build(),
                        "0d 4h 19m 20s"
                )
        );
    }

    @ParameterizedTest
    @MethodSource(value = "getSessionsForAtomicDurationCalculationTest")
    void whenSingleSession_getWorkSummary_shouldCorrectlyCalculateDuration(
            CashRegisterSession session,
            String expectedDuration
    ) {
        var from = LocalDateTime.of(2020, Month.JANUARY, 5, 16, 0);
        var to = from.plusHours(5);

        List<CashRegisterSession> sessions = List.of(session);

        Mockito.when(cashRegisterSessionRepository
                        .findByStoreIdThatIntersectInterval(testStore.getId(), from, to)
                )
                .thenReturn(sessions);

        var response = cashRegisterService.getWorkSummary(testStore.getId(), from, to);

        Assertions.assertEquals(expectedDuration, response.getDuration());
    }

    @Test
    void whenMultipleSessions_getWorkSummary_shouldCorrectlyCalculateDuration() {
        var from = LocalDateTime.of(2020, Month.JANUARY, 5, 16, 0);
        var to = from.plusHours(5);

        List<CashRegisterSession> sessions = List.of(
                // + 30m
                CashRegisterSession.builder()
                        .createdAt(from.plusHours(4).plusMinutes(30))
                        .closedAt(from.plusHours(5).plusMinutes(30))
                        .build(),
                // + 1h 30m 40s
                CashRegisterSession.builder()
                        .createdAt(from)
                        .closedAt(from.plusHours(1).plusMinutes(30).plusSeconds(40))
                        .build(),
                // + 5h
                CashRegisterSession.builder()
                        .createdAt(from.minusMinutes(40))
                        .closedAt(null)
                        .build()
        );

        Mockito.when(cashRegisterSessionRepository
                        .findByStoreIdThatIntersectInterval(testStore.getId(), from, to)
                )
                .thenReturn(sessions);

        var response = cashRegisterService.getWorkSummary(testStore.getId(), from, to);

        Assertions.assertEquals("0d 7h 0m 40s", response.getDuration());
    }
}
