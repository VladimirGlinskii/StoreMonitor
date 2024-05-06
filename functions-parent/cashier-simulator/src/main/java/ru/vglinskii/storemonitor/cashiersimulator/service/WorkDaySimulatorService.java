package ru.vglinskii.storemonitor.cashiersimulator.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.cashiersimulator.serviceclient.CashRegisterServiceClient;
import ru.vglinskii.storemonitor.cashiersimulator.dao.CashRegisterDao;
import ru.vglinskii.storemonitor.cashiersimulator.dao.CashierDao;
import ru.vglinskii.storemonitor.cashiersimulator.model.Cashier;
import static ru.vglinskii.storemonitor.cashiersimulator.utils.ApplicationConstants.CLOSE_CASH_REGISTER_PROBABILITY;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegister;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegisterSession;

public class WorkDaySimulatorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorkDaySimulatorService.class);
    private CashierDao cashierDao;
    private CashRegisterDao cashRegisterDao;
    private CashRegisterServiceClient cashRegisterServiceClient;
    private RandomGenerator randomGenerator;

    public WorkDaySimulatorService(
            CashierDao cashierDao,
            CashRegisterDao cashRegisterDao,
            CashRegisterServiceClient cashRegisterServiceClient,
            RandomGenerator randomGenerator
    ) {
        this.cashierDao = cashierDao;
        this.cashRegisterDao = cashRegisterDao;
        this.cashRegisterServiceClient = cashRegisterServiceClient;
        this.randomGenerator = randomGenerator;
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
        List<Cashier> cashiers = storeToCashiers.getOrDefault(storeId, new ArrayList<>());
        List<CashRegister> cashRegisters = storeToCashRegisters.getOrDefault(storeId, new ArrayList<>());

        for (var register : cashRegisters) {
            CashRegisterSession activeSession = register.getActiveSession();
            boolean isOpenedLessThanHour = activeSession != null &&
                    Duration.between(
                            activeSession.getCreatedAt(),
                            Instant.now()
                    ).toHours() < 1;
            if (isOpenedLessThanHour) {
                continue;
            }

            Cashier lessWorkedFreeCashier = cashiers.stream()
                    .filter(Cashier::isFree)
                    .findFirst()
                    .orElse(null);
            Cashier currentCashier = null;

            if (activeSession != null) {
                currentCashier = cashiers.stream()
                        .filter((c) -> c.getId() == activeSession.getCashierId())
                        .findFirst()
                        .orElse(null);
            }

            if (lessWorkedFreeCashier != null) {
                this.openCashRegister(register, currentCashier, lessWorkedFreeCashier);
            } else if (currentCashier != null && randomGenerator.nextFloat(0, 1) < CLOSE_CASH_REGISTER_PROBABILITY) {
                this.closeCashRegister(register, currentCashier);
            }
        }
    }

    private void openCashRegister(CashRegister cashRegister, Cashier currentCashier, Cashier newCashier) {
        LOGGER.info("Opening cash register {} by cashier {}", cashRegister.getId(), newCashier.getId());
        var isOpened = cashRegisterServiceClient.openCashRegister(cashRegister, newCashier);
        if (isOpened) {
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
        var isClosed = cashRegisterServiceClient.closeCashRegister(cashRegister, currentCashier);
        if (isClosed) {
            LOGGER.info("Cash register {} closed", cashRegister.getId());
            currentCashier.setFree(true);
        } else {
            LOGGER.error("Failed to close cash register {}", cashRegister.getId());
        }
    }
}
