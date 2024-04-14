package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vglinskii.storemonitor.baseapi.auth.AuthorizationContextHolder;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterStatusDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersWorkSummaryDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.baseapi.exception.ErrorCode;
import ru.vglinskii.storemonitor.baseapi.model.CashRegister;
import ru.vglinskii.storemonitor.baseapi.model.CashRegisterSession;
import ru.vglinskii.storemonitor.baseapi.repository.CashRegisterRepository;
import ru.vglinskii.storemonitor.baseapi.repository.CashRegisterSessionRepository;
import ru.vglinskii.storemonitor.baseapi.repository.EmployeeRepository;
import ru.vglinskii.storemonitor.baseapi.repository.StoreRepository;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

@Service
@Slf4j
public class CashRegisterService {
    private final CashRegisterRepository cashRegisterRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final CashRegisterSessionRepository cashRegisterSessionRepository;
    private final AuthorizationContextHolder authorizationContextHolder;

    public CashRegisterService(
            CashRegisterRepository cashRegisterRepository,
            StoreRepository storeRepository,
            EmployeeRepository employeeRepository,
            CashRegisterSessionRepository cashRegisterSessionRepository,
            AuthorizationContextHolder authorizationContextHolder
    ) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.storeRepository = storeRepository;
        this.employeeRepository = employeeRepository;
        this.cashRegisterSessionRepository = cashRegisterSessionRepository;
        this.authorizationContextHolder = authorizationContextHolder;
    }

    public CashRegisterDtoResponse create(CreateCashRegisterDtoRequest request) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        var store = storeRepository.getReferenceById(storeId);
        log.info("Creating cash register in store {}", storeId);

        if (cashRegisterRepository
                .findByStoreIdAndInventoryNumber(storeId, request.getInventoryNumber())
                .isPresent()
        ) {
            throw new AppRuntimeException(ErrorCode.CASH_REGISTER_EXISTS);
        }

        var cashRegister = CashRegister.builder()
                .inventoryNumber(request.getInventoryNumber())
                .store(store)
                .build();

        cashRegister = cashRegisterRepository.save(cashRegister);

        log.info("Created cash register {} in store {}", cashRegister.getId(), storeId);

        return CashRegisterDtoResponse.builder()
                .id(cashRegister.getId())
                .createdAt(cashRegister.getCreatedAt())
                .updatedAt(cashRegister.getUpdatedAt())
                .opened(false)
                .inventoryNumber(cashRegister.getInventoryNumber())
                .build();
    }

    public void delete(long id) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        log.info("Deleting cash register {} in store {}", id, storeId);
        cashRegisterRepository.deleteByIdAndStoreId(id, storeId);
        log.info("Deleted cash register {} in store {}", id, storeId);
    }

    @Transactional
    public void openSession(long registerId) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        var employeeId = authorizationContextHolder.getContext().getEmployeeId();
        log.info(
                "Opening cash register {} in store {} as employee {}",
                registerId, storeId, employeeId
        );

        var cashRegister = cashRegisterRepository
                .findByIdAndStoreId(registerId, storeId)
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASH_REGISTER_NOT_FOUND));
        var cashier = employeeRepository
                .findByIdAndStoreIdAndType(employeeId, storeId, EmployeeType.CASHIER)
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASHIER_NOT_FOUND));
        var previousSessionOptional = cashRegisterSessionRepository
                .findActiveByCashRegisterId(cashRegister.getId());

        if (previousSessionOptional.isPresent()) {
            var previousSession = previousSessionOptional.get();
            previousSession.setClosedAt(Instant.now());

            cashRegisterSessionRepository.save(previousSession);
            log.info(
                    "Closed cash register {} in store {} for employee {}",
                    registerId, storeId, previousSession.getCashier().getId()
            );
        }

        var newSession = CashRegisterSession.builder()
                .cashRegister(cashRegister)
                .cashier(cashier)
                .build();

        cashRegisterSessionRepository.save(newSession);

        log.info(
                "Opened cash register {} in store {} as employee {}",
                registerId, storeId, employeeId
        );
    }

    public void closeSession(long registerId) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        var employeeId = authorizationContextHolder.getContext().getEmployeeId();
        log.info(
                "Closing cash register {} in store {} as employee {}",
                registerId, storeId, employeeId
        );
        var cashRegister = cashRegisterRepository
                .findByIdAndStoreId(registerId, storeId)
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASH_REGISTER_NOT_FOUND));

        var session = cashRegisterSessionRepository
                .findActiveByCashRegisterId(cashRegister.getId())
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASH_REGISTER_ALREADY_CLOSED));

        if (!Objects.equals(session.getCashier().getId(), employeeId)) {
            throw new AppRuntimeException(ErrorCode.CASH_REGISTER_OPENED_BY_OTHER);
        }

        session.setClosedAt(Instant.now());
        cashRegisterSessionRepository.save(session);
        log.info(
                "Closed cash register {} in store {} as employee {}",
                registerId, storeId, employeeId
        );
    }

    public List<CashRegisterStatusDtoResponse> getStatuses() {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        log.info("Received get cash registers statuses request for store {}", storeId);

        return cashRegisterRepository.findWithLastSessionByStoreId(storeId)
                .stream()
                .map((cr) -> CashRegisterStatusDtoResponse.builder()
                        .id(cr.getId())
                        .inventoryNumber(cr.getInventoryNumber())
                        .opened(cr.getOpenedAt() != null && cr.getClosedAt() == null)
                        .build()
                )
                .collect(Collectors.toList());
    }

    public CashRegistersWorkSummaryDtoResponse getWorkSummary(
            Instant from,
            Instant rawTo
    ) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        log.info("Received get cash registers working summary request for store {}", storeId);
        Instant to = (rawTo.isAfter(Instant.now())) ? Instant.now() : rawTo;

        Duration totalDuration = cashRegisterSessionRepository
                .findByStoreIdThatIntersectInterval(storeId, from, to)
                .stream()
                .map((session) -> Duration.between(
                        (from.isBefore(session.getCreatedAt()))
                                ? session.getCreatedAt()
                                : from,
                        (session.getClosedAt() != null && to.isAfter(session.getClosedAt()))
                                ? session.getClosedAt()
                                : to
                ))
                .reduce(Duration.ZERO, Duration::plus);

        return new CashRegistersWorkSummaryDtoResponse(
                String.format(
                        "%dd %dh %dm %ds",
                        totalDuration.toDaysPart(),
                        totalDuration.toHoursPart(),
                        totalDuration.toMinutesPart(),
                        totalDuration.toSecondsPart()
                )
        );
    }
}
