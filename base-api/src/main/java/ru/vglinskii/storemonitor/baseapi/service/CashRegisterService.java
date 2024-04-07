package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterStatusDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersWorkSummaryDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.UpdateCashRegisterStatusDtoRequest;
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
public class CashRegisterService {
    private final CashRegisterRepository cashRegisterRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final CashRegisterSessionRepository cashRegisterSessionRepository;

    public CashRegisterService(
            CashRegisterRepository cashRegisterRepository,
            StoreRepository storeRepository,
            EmployeeRepository employeeRepository, CashRegisterSessionRepository cashRegisterSessionRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.storeRepository = storeRepository;
        this.employeeRepository = employeeRepository;
        this.cashRegisterSessionRepository = cashRegisterSessionRepository;
    }

    public CashRegisterDtoResponse create(long storeId, CreateCashRegisterDtoRequest request) {
        var store = storeRepository.findById(storeId).get();

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

        return CashRegisterDtoResponse.builder()
                .id(cashRegister.getId())
                .createdAt(cashRegister.getCreatedAt())
                .updatedAt(cashRegister.getUpdatedAt())
                .opened(false)
                .inventoryNumber(cashRegister.getInventoryNumber())
                .build();
    }

    public void delete(long storeId, long id) {
        cashRegisterRepository.deleteByIdAndStoreId(id, storeId);
    }

    @Transactional
    public void openSession(
            long storeId,
            long registerId,
            UpdateCashRegisterStatusDtoRequest request
    ) {
        var cashRegister = cashRegisterRepository
                .findByIdAndStoreId(registerId, storeId)
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASH_REGISTER_NOT_FOUND));
        var cashier = employeeRepository
                .findByIdAndStoreIdAndType(request.getCashierId(), storeId, EmployeeType.CASHIER)
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASHIER_NOT_FOUND));
        var previousSessionOptional = cashRegisterSessionRepository
                .findActiveByCashRegisterId(cashRegister.getId());

        if (previousSessionOptional.isPresent()) {
            var previousSession = previousSessionOptional.get();
            previousSession.setClosedAt(Instant.now());

            cashRegisterSessionRepository.save(previousSession);
        }

        var newSession = CashRegisterSession.builder()
                .cashRegister(cashRegister)
                .cashier(cashier)
                .build();

        cashRegisterSessionRepository.save(newSession);
    }

    public void closeSession(
            long storeId,
            long registerId,
            UpdateCashRegisterStatusDtoRequest request
    ) {
        var cashRegister = cashRegisterRepository
                .findByIdAndStoreId(registerId, storeId)
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASH_REGISTER_NOT_FOUND));
        var cashier = employeeRepository
                .findByIdAndStoreIdAndType(request.getCashierId(), storeId, EmployeeType.CASHIER)
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASHIER_NOT_FOUND));

        var session = cashRegisterSessionRepository
                .findActiveByCashRegisterId(cashRegister.getId())
                .orElseThrow(() -> new AppRuntimeException(ErrorCode.CASH_REGISTER_ALREADY_CLOSED));

        if (!Objects.equals(session.getCashier().getId(), request.getCashierId())) {
            throw new AppRuntimeException(ErrorCode.CASH_REGISTER_OPENED_BY_OTHER);
        }

        session.setClosedAt(Instant.now());
        cashRegisterSessionRepository.save(session);
    }

    public List<CashRegisterStatusDtoResponse> getStatuses(long storeId) {
        return cashRegisterRepository.findWithLastSessionByStoreId(storeId)
                .stream()
                .map((cr) -> CashRegisterStatusDtoResponse.builder()
                        .id(cr.getId())
                        .inventoryNumber(cr.getInventoryNumber())
                        .opened(cr.getOpenedAt() != null && cr.getClosedAt() == null)
                        .build())
                .collect(Collectors.toList());
    }

    public CashRegistersWorkSummaryDtoResponse getWorkSummary(
            long storeId,
            Instant from,
            Instant rawTo
    ) {
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
