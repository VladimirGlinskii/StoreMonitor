package ru.vglinskii.storemonitor.cashiersimulator;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.cashiersimulator.api.CashRegisterApi;
import ru.vglinskii.storemonitor.cashiersimulator.config.ApplicationProperties;
import ru.vglinskii.storemonitor.cashiersimulator.dao.CashRegisterDao;
import ru.vglinskii.storemonitor.cashiersimulator.dao.CashierDao;
import ru.vglinskii.storemonitor.cashiersimulator.service.WorkDaySimulatorService;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.dto.TriggerRequestDto;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<TriggerRequestDto, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private ApplicationProperties properties;
    private DatabaseConnectivity databaseConnectivity;
    private WorkDaySimulatorService workDaySimulatorService;

    public Handler() {
        this(new ApplicationProperties());
    }

    public Handler(ApplicationProperties properties) {
        this.properties = properties;

        var dbProps = new Properties();
        dbProps.setProperty("ssl", "true");
        dbProps.setProperty("user", properties.getDbUser());
        dbProps.setProperty("password", properties.getDbPassword());
        this.databaseConnectivity = new DatabaseConnectivity(properties.getDbUrl(), dbProps);

        var cashierDao = new CashierDao(databaseConnectivity);
        var cashRegisterDao = new CashRegisterDao(databaseConnectivity);
        var objectMapper = new AppObjectMapper();
        var cashRegisterApi = new CashRegisterApi(
                objectMapper,
                properties.getBaseApiUrl()
        );
        this.workDaySimulatorService = new WorkDaySimulatorService(cashierDao, cashRegisterDao, cashRegisterApi);
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