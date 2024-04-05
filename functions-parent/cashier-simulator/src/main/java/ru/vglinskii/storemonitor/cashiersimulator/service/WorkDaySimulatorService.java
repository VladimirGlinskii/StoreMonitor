package ru.vglinskii.storemonitor.cashiersimulator.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.cashiersimulator.api.CashRegisterApi;
import ru.vglinskii.storemonitor.cashiersimulator.dao.CashRegisterDao;
import ru.vglinskii.storemonitor.cashiersimulator.dao.CashierDao;
import ru.vglinskii.storemonitor.cashiersimulator.model.CashRegister;
import ru.vglinskii.storemonitor.cashiersimulator.model.Cashier;

public class WorkDaySimulatorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorkDaySimulatorService.class);
    private CashierDao cashierDao;
    private CashRegisterDao cashRegisterDao;
    private CashRegisterApi cashRegisterApi;

    public WorkDaySimulatorService(
            CashierDao cashierDao,
            CashRegisterDao cashRegisterDao,
            CashRegisterApi cashRegisterApi
    ) {
        this.cashierDao = cashierDao;
        this.cashRegisterDao = cashRegisterDao;
        this.cashRegisterApi = cashRegisterApi;
    }

    public void updateCashRegistersStates() {
        var storeToCashiers = cashierDao.findAllOrderedByActivity()
                .stream()
                .collect(Collectors.groupingBy(Cashier::getStoreId));
        var storeToCashRegisters = cashRegisterDao.findAllWithDaySessions()
                .stream()
                .collect(Collectors.groupingBy(CashRegister::getStoreId));

        storeToCashRegisters.forEach(((storeId, cashRegisters) -> updateCashRegistersStatesForStore(
                storeId,
                storeToCashiers,
                storeToCashRegisters
        )));
    }

    private void updateCashRegistersStatesForStore(
            long storeId,
            Map<Long, List<Cashier>> storeToCashiers,
            Map<Long, List<CashRegister>> storeToCashRegisters
    ) {
        var cashiers = storeToCashiers.getOrDefault(storeId, new ArrayList<>());
        var cashRegisters = storeToCashRegisters.getOrDefault(storeId, new ArrayList<>());

        for (var register : cashRegisters) {
            var lastSession = register.getLastSession();
            var isOpenedLessThanHour = lastSession != null &&
                    lastSession.getClosedAt() == null &&
                    Duration.between(
                            register.getDaySessions().getLast().getCreatedAt(),
                            LocalDateTime.now()
                    ).toHours() < 1;
            if (isOpenedLessThanHour) {
                continue;
            }

            var lessWorkedFreeCashier = cashiers.stream()
                    .filter(Cashier::isFree)
                    .findFirst()
                    .orElse(null);
            var currentCashier = (lastSession == null)
                    ? null
                    : cashiers.stream()
                    .filter((c) -> c.getId() == lastSession.getCashierId())
                    .findFirst()
                    .orElse(null);

            if (lessWorkedFreeCashier != null) {
                this.openCashRegister(register, currentCashier, lessWorkedFreeCashier);
            } else if (currentCashier != null && Math.random() < 0.25) {
                this.closeCashRegister(register, currentCashier);
            }
        }
    }

    private void openCashRegister(CashRegister cashRegister, Cashier currentCashier, Cashier newCashier) {
        LOGGER.info("Opening cash register {} by cashier {}", cashRegister.getId(), newCashier.getId());
        var response = cashRegisterApi.openCashRegister(cashRegister, newCashier);
        if (response.is2xxSuccessful()) {
            LOGGER.info("Cash register {} opened", cashRegister.getId());
            newCashier.setFree(false);
            if (currentCashier != null) {
                currentCashier.setFree(true);
            }
        } else {
            LOGGER.error("Failed to open cash register {}", cashRegister.getId());
        }
    }

    private void closeCashRegister(CashRegister cashRegister, Cashier currentCashier) {
        LOGGER.info("Closing cash register {} by cashier {}", cashRegister.getId(), currentCashier.getId());
        var response = cashRegisterApi.closeCashRegister(cashRegister, currentCashier);
        if (response.is2xxSuccessful()) {
            LOGGER.info("Cash register {} closed", cashRegister.getId());
            currentCashier.setFree(true);
        } else {
            LOGGER.error("Failed to close cash register {}", cashRegister.getId());
        }
    }
}