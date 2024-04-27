package ru.vglinskii.storemonitor.sensorsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.TriggerRequestDto;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import ru.vglinskii.storemonitor.sensorsimulator.api.DevicesApi;
import ru.vglinskii.storemonitor.sensorsimulator.config.ApplicationProperties;
import ru.vglinskii.storemonitor.sensorsimulator.config.SensorSimulatorConfig;
import ru.vglinskii.storemonitor.sensorsimulator.dao.SensorDao;
import ru.vglinskii.storemonitor.sensorsimulator.service.SensorSimulatorService;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<TriggerRequestDto, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private DatabaseConnectivity databaseConnectivity;
    private SensorSimulatorService sensorSimulatorService;

    public Handler() {
        var properties = new ApplicationProperties();
        var objectMapper = new AppObjectMapper();
        init(
                DatabaseConnectivityFactory.create(properties),
                new DevicesApi(
                        objectMapper,
                        properties.getDevicesApiUrl()
                ),
                SensorSimulatorConfig.builder()
                        .sensorValueCelsiusMean(properties.getSensorValueCelsiusMean())
                        .sensorValueCelsiusStandardDeviation(properties.getSensorValueCelsiusStandardDeviation())
                        .build()
        );
    }

    public Handler(
            DatabaseConnectivity databaseConnectivity,
            DevicesApi devicesApi,
            SensorSimulatorConfig simulatorConfig
    ) {
        init(databaseConnectivity, devicesApi, simulatorConfig);
    }

    public void init(
            DatabaseConnectivity databaseConnectivity,
            DevicesApi devicesApi,
            SensorSimulatorConfig simulatorConfig
    ) {
        this.databaseConnectivity = databaseConnectivity;

        var sensorDao = new SensorDao(databaseConnectivity);

        this.sensorSimulatorService = new SensorSimulatorService(
                sensorDao,
                devicesApi,
                simulatorConfig
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
