package ru.vglinskii.storemonitor.cashiersimulator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vglinskii.storemonitor.cashiersimulator.serviceclient.CashRegisterServiceClient;
import ru.vglinskii.storemonitor.cashiersimulator.model.Cashier;
import ru.vglinskii.storemonitor.cashiersimulator.utils.ApplicationConstants;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;
import ru.vglinskii.storemonitor.functionscommon.http.HttpResponse;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonCashRegisterDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonCashRegisterSessionDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonEmployeeDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonStoreDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegister;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegisterSession;
import ru.vglinskii.storemonitor.functionscommon.model.Employee;
import ru.vglinskii.storemonitor.functionscommon.model.Store;
import ru.vglinskii.storemonitor.functionscommon.utils.TestContext;

@ExtendWith(MockitoExtension.class)
public class CashierSimulatorIT {
    private static final Instant BASE_DATE = Instant.parse("2020-01-01T00:00:00Z");
    private Handler handler;
    private CashRegisterServiceClient cashRegisterServiceClient;
    private RandomGenerator randomGenerator;
    private CommonStoreDao storeDao;
    private CommonEmployeeDao employeeDao;
    private CommonCashRegisterDao cashRegisterDao;
    private CommonCashRegisterSessionDao commonCashRegisterSessionDao;

    private Store store1;
    private Employee cashier1InStore1;
    private Employee cashier2InStore1;
    private CashRegister cashRegister1InStore1;
    private CashRegister cashRegister2InStore1;

    public CashierSimulatorIT() {
        this.cashRegisterServiceClient = Mockito.mock(CashRegisterServiceClient.class);
        this.randomGenerator = Mockito.mock(RandomGenerator.class);
        var databaseConnectivity = DatabaseConnectivityFactory.create(
                "jdbc:mysql://localhost:3306/store-monitor-test",
                "root",
                "root"
        );
        this.handler = new Handler(databaseConnectivity, cashRegisterServiceClient, randomGenerator);
        this.storeDao = new CommonStoreDao(databaseConnectivity);
        this.employeeDao = new CommonEmployeeDao(databaseConnectivity);
        this.cashRegisterDao = new CommonCashRegisterDao(databaseConnectivity);
        this.commonCashRegisterSessionDao = new CommonCashRegisterSessionDao(databaseConnectivity);
    }

    @BeforeEach
    void init() {
        employeeDao.deleteAll();
        storeDao.deleteAll();

        store1 = storeDao.insert(
                Store.builder()
                        .location("Location 1")
                        .createdAt(BASE_DATE.minus(10, ChronoUnit.DAYS))
                        .build()
        );

        cashier1InStore1 = createCashier(store1);
        cashier2InStore1 = createCashier(store1);

        cashRegister1InStore1 = cashRegisterDao.insert(
                CashRegister.builder()
                        .storeId(store1.getId())
                        .inventoryNumber("00000001")
                        .createdAt(BASE_DATE.minus(10, ChronoUnit.DAYS))
                        .build()
        );
        cashRegister2InStore1 = cashRegisterDao.insert(
                CashRegister.builder()
                        .storeId(store1.getId())
                        .inventoryNumber("00000002")
                        .createdAt(BASE_DATE.minus(10, ChronoUnit.DAYS))
                        .build()
        );

        // Yesterday sessions
        createCashRegisterSession(
                cashRegister1InStore1,
                cashier1InStore1,
                BASE_DATE.minus(1, ChronoUnit.DAYS),
                BASE_DATE.minus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.HOURS)
        );

        // Today sessions in store 1
        createCashRegisterSession(
                cashRegister1InStore1,
                cashier1InStore1,
                BASE_DATE,
                BASE_DATE.plus(2, ChronoUnit.HOURS)
        );
        createCashRegisterSession(
                cashRegister2InStore1,
                cashier2InStore1,
                BASE_DATE,
                BASE_DATE.plus(4, ChronoUnit.HOURS)
        );
    }

    private void createCashRegisterSession(
            CashRegister cashRegister,
            Employee cashier,
            Instant from,
            Instant to
    ) {
        commonCashRegisterSessionDao.insert(
                CashRegisterSession.builder()
                        .cashRegisterId(cashRegister.getId())
                        .cashierId(cashier.getId())
                        .createdAt(from)
                        .closedAt(to)
                        .build()
        );
    }

    private Employee createCashier(Store store) {
        return employeeDao.insert(
                Employee.builder()
                        .firstName("Firstname")
                        .lastName("Lastname")
                        .type(EmployeeType.CASHIER)
                        .storeId(store.getId())
                        .secret(UUID.randomUUID().toString())
                        .createdAt(BASE_DATE.minus(10, ChronoUnit.DAYS))
                        .build()
        );
    }

    @Test
    void whenWorkLessThanHour_simulate_shouldDoNothing() {
        createCashRegisterSession(
                cashRegister1InStore1,
                cashier1InStore1,
                BASE_DATE.plus(4, ChronoUnit.HOURS),
                null
        );
        createCashRegisterSession(
                cashRegister2InStore1,
                cashier2InStore1,
                BASE_DATE.plus(4, ChronoUnit.HOURS),
                null
        );
        var now = BASE_DATE
                .plus(4, ChronoUnit.HOURS)
                .plus(30, ChronoUnit.MINUTES);
        try (var mockedInstant = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(now);

            triggerSimulation();

            Mockito.verify(cashRegisterServiceClient, Mockito.never()).closeCashRegister(Mockito.any(), Mockito.any());
            Mockito.verify(randomGenerator, Mockito.never()).nextFloat(Mockito.anyFloat(), Mockito.anyFloat());
        }
    }

    @Test
    void whenClosed_simulate_shouldOpenForLessWorkedCashier() {
        var cashier3InStore1 = createCashier(store1);
        createCashRegisterSession(
                cashRegister1InStore1,
                cashier1InStore1,
                BASE_DATE.plus(4, ChronoUnit.HOURS),
                null
        );
        var now = BASE_DATE
                .plus(4, ChronoUnit.HOURS)
                .plus(30, ChronoUnit.MINUTES);
        try (var mockedInstant = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            var cashRegisterCaptor = ArgumentCaptor.forClass(CashRegister.class);
            var cashierCaptor = ArgumentCaptor.forClass(Cashier.class);
            mockedInstant.when(Instant::now).thenReturn(now);
            Mockito.when(cashRegisterServiceClient.openCashRegister(cashRegisterCaptor.capture(), cashierCaptor.capture()))
                    .thenReturn(true);

            triggerSimulation();

            Assertions.assertEquals(cashRegister2InStore1.getId(), cashRegisterCaptor.getValue().getId());
            Assertions.assertEquals(cashier3InStore1.getId(), cashierCaptor.getValue().getId());
            Assertions.assertEquals(cashier3InStore1.getSecret(), cashierCaptor.getValue().getSecret());
        }
    }

    @Test
    void whenOpenedMoreThanHourAndProbabilityLessThanThreshold_simulate_shouldClose() {
        createCashRegisterSession(
                cashRegister1InStore1,
                cashier1InStore1,
                BASE_DATE.plus(4, ChronoUnit.HOURS),
                null
        );
        createCashRegisterSession(
                cashRegister2InStore1,
                cashier2InStore1,
                BASE_DATE.plus(5, ChronoUnit.HOURS),
                null
        );
        var now = BASE_DATE.plus(5, ChronoUnit.HOURS)
                .plus(30, ChronoUnit.MINUTES);
        try (var mockedInstant = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            var cashRegisterCaptor = ArgumentCaptor.forClass(CashRegister.class);
            var cashierCaptor = ArgumentCaptor.forClass(Cashier.class);
            mockedInstant.when(Instant::now).thenReturn(now);
            Mockito.when(cashRegisterServiceClient.closeCashRegister(cashRegisterCaptor.capture(), cashierCaptor.capture()))
                    .thenReturn(true);
            Mockito.when(randomGenerator.nextFloat(Mockito.anyFloat(), Mockito.anyFloat()))
                    .thenReturn(ApplicationConstants.CLOSE_CASH_REGISTER_PROBABILITY - 0.01f);

            triggerSimulation();

            Assertions.assertEquals(cashRegister1InStore1.getId(), cashRegisterCaptor.getValue().getId());
            Assertions.assertEquals(cashier1InStore1.getId(), cashierCaptor.getValue().getId());
            Assertions.assertEquals(cashier1InStore1.getSecret(), cashierCaptor.getValue().getSecret());
        }
    }

    @Test
    void whenOpenedMoreThanHourAndProbabilityGreaterThanThreshold_simulate_shouldDoNothing() {
        createCashRegisterSession(
                cashRegister1InStore1,
                cashier1InStore1,
                BASE_DATE.plus(4, ChronoUnit.HOURS),
                null
        );
        createCashRegisterSession(
                cashRegister2InStore1,
                cashier2InStore1,
                BASE_DATE.plus(4, ChronoUnit.HOURS),
                null
        );
        var now = BASE_DATE.plus(5, ChronoUnit.HOURS)
                .plus(30, ChronoUnit.MINUTES);
        try (var mockedInstant = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(now);
            Mockito.when(randomGenerator.nextFloat(Mockito.anyFloat(), Mockito.anyFloat()))
                    .thenReturn(ApplicationConstants.CLOSE_CASH_REGISTER_PROBABILITY + 0.01f);

            triggerSimulation();

            Mockito.verify(cashRegisterServiceClient, Mockito.never()).closeCashRegister(Mockito.any(), Mockito.any());
        }
    }

    private void triggerSimulation() {
        handler.handle(null, new TestContext());
    }
}
