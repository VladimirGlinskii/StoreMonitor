package ru.vglinskii.storemonitor.cashiersimulator;

import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.cashiersimulator.serviceclient.CashRegisterServiceClient;
import ru.vglinskii.storemonitor.cashiersimulator.config.ApplicationProperties;
import ru.vglinskii.storemonitor.cashiersimulator.dao.CashRegisterDao;
import ru.vglinskii.storemonitor.cashiersimulator.dao.CashierDao;
import ru.vglinskii.storemonitor.cashiersimulator.service.WorkDaySimulatorService;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.TriggerRequestDto;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<TriggerRequestDto, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private DatabaseConnectivity databaseConnectivity;
    private WorkDaySimulatorService workDaySimulatorService;

    public Handler() {
        var properties = new ApplicationProperties();
        init(
                DatabaseConnectivityFactory.create(properties),
                new CashRegisterServiceClient(properties.getBaseApiUrl()),
                new SplittableRandom()
        );
    }

    public Handler(
            DatabaseConnectivity databaseConnectivity,
            CashRegisterServiceClient cashRegisterServiceClient,
            RandomGenerator randomGenerator
    ) {
        init(databaseConnectivity, cashRegisterServiceClient, randomGenerator);
    }

    private void init(
            DatabaseConnectivity databaseConnectivity,
            CashRegisterServiceClient cashRegisterServiceClient,
            RandomGenerator randomGenerator
    ) {
        this.databaseConnectivity = databaseConnectivity;
        var cashierDao = new CashierDao(databaseConnectivity);
        var cashRegisterDao = new CashRegisterDao(databaseConnectivity);
        this.workDaySimulatorService = new WorkDaySimulatorService(
                cashierDao,
                cashRegisterDao,
                cashRegisterServiceClient,
                randomGenerator
        );
    }

    @Override
    public String handle(TriggerRequestDto request, Context context) {
        try {
            LOGGER.info("Received request from trigger {}", request);

            workDaySimulatorService.updateCashRegistersStates();

            return "";
        } catch (Throwable e) {
            LOGGER.error("Unhandled exception", e);

            throw e;
        }
    }
}
