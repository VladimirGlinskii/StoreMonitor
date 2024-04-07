package ru.vglinskii.storemonitor.sensorsimulator;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.dto.TriggerRequestDto;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import ru.vglinskii.storemonitor.sensorsimulator.api.DevicesApi;
import ru.vglinskii.storemonitor.sensorsimulator.config.ApplicationProperties;
import ru.vglinskii.storemonitor.sensorsimulator.dao.SensorDao;
import ru.vglinskii.storemonitor.sensorsimulator.service.SensorSimulatorService;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<TriggerRequestDto, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private ApplicationProperties properties;
    private DatabaseConnectivity databaseConnectivity;
    private SensorSimulatorService sensorSimulatorService;

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

        var sensorDao = new SensorDao(databaseConnectivity);
        var objectMapper = new AppObjectMapper();
        var devicesApi = new DevicesApi(
                objectMapper,
                properties.getDevicesApiUrl()
        );
        this.sensorSimulatorService = new SensorSimulatorService(
                sensorDao,
                devicesApi,
                properties
        );
    }

    @Override
    public String handle(TriggerRequestDto request, Context context) {
        try {
            LOGGER.info("Received request from trigger {}", request);

            sensorSimulatorService.updateSensorsValues();

            return "";
        } catch (Throwable e) {
            LOGGER.error("Unhandled exception", e);

            throw e;
        }
    }
}
