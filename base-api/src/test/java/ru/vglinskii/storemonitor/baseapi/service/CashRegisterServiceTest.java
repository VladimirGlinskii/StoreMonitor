package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterStatusDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersStatusesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.baseapi.exception.ErrorCode;
import ru.vglinskii.storemonitor.baseapi.mapper.CashRegisterMapper;
import ru.vglinskii.storemonitor.baseapi.mapper.CashRegisterMapperImpl;
import ru.vglinskii.storemonitor.baseapi.model.CashRegisterSession;
import ru.vglinskii.storemonitor.baseapi.projection.CashRegisterStatusProjection;
import ru.vglinskii.storemonitor.baseapi.projection.CashRegisterStatusProjectionTestImpl;
import ru.vglinskii.storemonitor.baseapi.repository.CashRegisterRepository;
import ru.vglinskii.storemonitor.baseapi.repository.CashRegisterSessionRepository;
import ru.vglinskii.storemonitor.baseapi.repository.EmployeeRepository;
import ru.vglinskii.storemonitor.baseapi.repository.StoreRepository;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

@ExtendWith(MockitoExtension.class)
public class CashRegisterServiceTest extends ServiceTestBase {
    @Mock
    private CashRegisterRepository cashRegisterRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CashRegisterSessionRepository cashRegisterSessionRepository;
    @Spy
    private CashRegisterMapper cashRegisterMapper = new CashRegisterMapperImpl();
    @InjectMocks
    private CashRegisterService cashRegisterService;

    public CashRegisterServiceTest() {
        super();
    }

    @Test
    void whenNotExists_create_shouldSuccess() {
        authorizeAs(testDirector);
        var expectedCashRegister = testDataGenerator.createCashRegister(1, testStore);
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber(expectedCashRegister.getInventoryNumber())
                .build();
        var expectedResponse = cashRegisterMapper.toRegisterDto(expectedCashRegister);

        Mockito.when(storeRepository.getReferenceById(testStore.getId()))
                .thenReturn(testStore);
        Mockito.when(cashRegisterRepository.findByStoreIdAndInventoryNumber(
                        testStore.getId(),
                        expectedCashRegister.getInventoryNumber()
                ))
                .thenReturn(Optional.empty());
        Mockito.when(cashRegisterRepository.save(Mockito.any()))
                .thenReturn(expectedCashRegister);

        var response = cashRegisterService.create(request);

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    void whenExists_create_shouldThrowException() {
        authorizeAs(testDirector);
        var existingCashRegister = testDataGenerator.createCashRegister(10, testStore);
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber(existingCashRegister.getInventoryNumber())
                .build();

        Mockito.when(storeRepository.getReferenceById(testStore.getId()))
                .thenReturn(testStore);
        Mockito.when(cashRegisterRepository.findByStoreIdAndInventoryNumber(
                        testStore.getId(),
                        existingCashRegister.getInventoryNumber()
                ))
                .thenReturn(Optional.of(existingCashRegister));

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.create(request)
        );

        Mockito.verify(cashRegisterRepository, Mockito.never()).save(Mockito.any());
        Assertions.assertEquals(ErrorCode.CASH_REGISTER_EXISTS, exception.getErrorCode());
    }

    @Test
    void delete_shouldDeleteInStore() {
        authorizeAs(testDirector);
        long targetCashRegisterId = 10;

        cashRegisterService.delete(targetCashRegisterId);

        Mockito.verify(cashRegisterRepository, Mockito.times(1))
                .deleteByIdAndStoreId(targetCashRegisterId, testStore.getId());
    }

    @Test
    void whenValidAndNoSession_openSession_shouldCreateSession() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        authorizeAs(cashier);
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

        cashRegisterService.openSession(cashRegister.getId());

        var sessionCaptor = ArgumentCaptor.forClass(CashRegisterSession.class);
        Mockito.verify(cashRegisterSessionRepository, Mockito.times(1))
                .save(sessionCaptor.capture());
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
        authorizeAs(cashier);

        var expectedSessionId = 5L;
        var existingSession = testDataGenerator.createCashRegisterSession(
                expectedSessionId - 1,
                cashRegister,
                cashier
        );

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

        cashRegisterService.openSession(cashRegister.getId());

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
        authorizeAs(cashier);

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.openSession(cashRegister.getId())
        );

        Assertions.assertEquals(ErrorCode.CASH_REGISTER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void whenCashierNotExists_openSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        authorizeAs(cashier);

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
                () -> cashRegisterService.openSession(cashRegister.getId())
        );

        Assertions.assertEquals(ErrorCode.CASHIER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void whenValidAndSessionExists_closeSession_shouldCloseSession() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        authorizeAs(cashier);

        var existingSession = testDataGenerator.createCashRegisterSession(
                5,
                cashRegister,
                cashier
        );

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(cashRegisterSessionRepository.findActiveByCashRegisterId(cashRegister.getId()))
                .thenReturn(Optional.of(existingSession));
        Mockito.when(cashRegisterSessionRepository.save(Mockito.any()))
                .thenAnswer((i) -> i.getArguments()[0]);

        cashRegisterService.closeSession(cashRegister.getId());

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
        authorizeAs(cashier);

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(cashRegisterSessionRepository.findActiveByCashRegisterId(cashRegister.getId()))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.closeSession(cashRegister.getId())
        );

        Assertions.assertEquals(ErrorCode.CASH_REGISTER_ALREADY_CLOSED, exception.getErrorCode());
    }

    @Test
    void whenCashRegisterNotExists_closeSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        authorizeAs(cashier);

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.closeSession(cashRegister.getId())
        );

        Assertions.assertEquals(ErrorCode.CASH_REGISTER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void whenSessionOpenedByOther_closeSession_shouldThrowException() {
        var cashRegister = testDataGenerator.createCashRegister(10, testStore);
        var cashier = testDataGenerator.createEmployee(100, testStore, EmployeeType.CASHIER);
        authorizeAs(cashier);
        var otherCashier = testDataGenerator.createEmployee(101, testStore, EmployeeType.CASHIER);
        var existingSession = testDataGenerator.createCashRegisterSession(
                5,
                cashRegister,
                otherCashier
        );

        Mockito.when(cashRegisterRepository.findByIdAndStoreId(cashRegister.getId(), testStore.getId()))
                .thenReturn(Optional.of(cashRegister));
        Mockito.when(cashRegisterSessionRepository.findActiveByCashRegisterId(cashRegister.getId()))
                .thenReturn(Optional.of(existingSession));

        var exception = Assertions.assertThrows(
                AppRuntimeException.class,
                () -> cashRegisterService.closeSession(cashRegister.getId())
        );

        Assertions.assertEquals(ErrorCode.CASH_REGISTER_OPENED_BY_OTHER, exception.getErrorCode());
    }

    @Test
    void getStatuses_shouldReturnCorrectResponse() {
        authorizeAs(testDirector);
        List<CashRegisterStatusProjection> projections = List.of(
                new CashRegisterStatusProjectionTestImpl(1, "1", Instant.now(), null),
                new CashRegisterStatusProjectionTestImpl(1, "1", Instant.now(), Instant.now()),
                new CashRegisterStatusProjectionTestImpl(1, "1", null, null)
        );
        var expectedResponse = new CashRegistersStatusesDtoResponse(
                List.of(
                        new CashRegisterStatusDtoResponse(1, "1", true),
                        new CashRegisterStatusDtoResponse(1, "1", false),
                        new CashRegisterStatusDtoResponse(1, "1", false)
                )
        );

        Mockito.when(cashRegisterRepository.findWithLastSessionByStoreId(testStore.getId()))
                .thenReturn(projections);

        var response = cashRegisterService.getStatuses();

        Assertions.assertEquals(expectedResponse, response);
    }

    private static Stream<Arguments> getSessionsForAtomicDurationCalculationTest() {
        var from = Instant.parse("2020-01-05T16:00:00Z");
        var to = from.plus(5, ChronoUnit.HOURS);

        return Stream.of(
                // Opened before interval and closed inside
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.minus(30, ChronoUnit.MINUTES))
                                .closedAt(from.plus(2, ChronoUnit.HOURS).plus(20, ChronoUnit.SECONDS))
                                .build(),
                        "0d 2h 0m 20s"
                ),
                // Opened before interval and closed after
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.minus(30, ChronoUnit.MINUTES))
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
                                .closedAt(from.plus(1, ChronoUnit.HOURS)
                                        .plus(30, ChronoUnit.MINUTES)
                                        .plus(40, ChronoUnit.SECONDS))
                                .build(),
                        "0d 1h 30m 40s"
                ),
                // Opened inside interval and closed after
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.plus(4, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES))
                                .closedAt(from.plus(5, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES))
                                .build(),
                        "0d 0h 30m 0s"
                ),
                // Opened inside interval and closed inside
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.plus(3, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES))
                                .closedAt(from.plus(4, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES))
                                .build(),
                        "0d 0h 40m 0s"
                ),
                // Opened before interval and not closed
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.minus(40, ChronoUnit.MINUTES))
                                .closedAt(null)
                                .build(),
                        "0d 5h 0m 0s"
                ),
                // Opened inside interval and not closed
                Arguments.of(
                        CashRegisterSession.builder()
                                .createdAt(from.plus(40, ChronoUnit.MINUTES).plusSeconds(40))
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
        authorizeAs(testDirector);
        var from = Instant.parse("2020-01-05T16:00:00Z");
        var to = from.plus(5, ChronoUnit.HOURS);

        List<CashRegisterSession> sessions = List.of(session);

        Mockito.when(cashRegisterSessionRepository
                        .findByStoreIdThatIntersectInterval(testStore.getId(), from, to)
                )
                .thenReturn(sessions);

        var response = cashRegisterService.getWorkSummary(from, to);

        Assertions.assertEquals(expectedDuration, response.getDuration());
    }

    @Test
    void whenMultipleSessions_getWorkSummary_shouldCorrectlyCalculateDuration() {
        authorizeAs(testDirector);
        var from = Instant.parse("2020-01-05T16:00:00Z");
        var to = from.plus(5, ChronoUnit.HOURS);

        List<CashRegisterSession> sessions = List.of(
                // + 30m
                CashRegisterSession.builder()
                        .createdAt(from.plus(4, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES))
                        .closedAt(from.plus(5, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES))
                        .build(),
                // + 1h 30m 40s
                CashRegisterSession.builder()
                        .createdAt(from)
                        .closedAt(from.plus(1, ChronoUnit.HOURS)
                                .plus(30, ChronoUnit.MINUTES)
                                .plusSeconds(40))
                        .build(),
                // + 5h
                CashRegisterSession.builder()
                        .createdAt(from.minus(40, ChronoUnit.MINUTES))
                        .closedAt(null)
                        .build()
        );

        Mockito.when(cashRegisterSessionRepository
                        .findByStoreIdThatIntersectInterval(testStore.getId(), from, to)
                )
                .thenReturn(sessions);

        var response = cashRegisterService.getWorkSummary(from, to);

        Assertions.assertEquals("0d 7h 0m 40s", response.getDuration());
    }

    @Test
    void whenToDateInFuture_getWorkSummary_shouldUseNowAsToDate() {
        authorizeAs(testDirector);
        var now = Instant.now();
        try (var instantMock = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            instantMock.when(Instant::now).thenReturn(now);
            var from = now.minus(2, ChronoUnit.HOURS);
            var to = from.plus(5, ChronoUnit.DAYS);

            List<CashRegisterSession> sessions = List.of(
                    CashRegisterSession.builder()
                            .createdAt(from)
                            .closedAt(null)
                            .build()
            );

            Mockito.when(cashRegisterSessionRepository
                            .findByStoreIdThatIntersectInterval(testStore.getId(), from, now)
                    )
                    .thenReturn(sessions);

            var response = cashRegisterService.getWorkSummary(from, to);

            Assertions.assertEquals("0d 2h 0m 0s", response.getDuration());
        }
    }
}
