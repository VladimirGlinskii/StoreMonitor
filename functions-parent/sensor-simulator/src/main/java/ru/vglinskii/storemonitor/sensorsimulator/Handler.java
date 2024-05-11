package ru.vglinskii.storemonitor.sensorsimulator;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.TriggerRequestDto;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import ru.vglinskii.storemonitor.sensorsimulator.config.ApplicationProperties;
import ru.vglinskii.storemonitor.sensorsimulator.config.SensorSimulatorConfig;
import ru.vglinskii.storemonitor.sensorsimulator.dao.SensorDao;
import ru.vglinskii.storemonitor.sensorsimulator.service.SensorSimulatorService;
import ru.vglinskii.storemonitor.sensorsimulator.serviceclient.SensorServiceClient;
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
                new SensorServiceClient(
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
            SensorServiceClient sensorServiceClient,
            SensorSimulatorConfig simulatorConfig
    ) {
        init(databaseConnectivity, sensorServiceClient, simulatorConfig);
    }

    public void init(
            DatabaseConnectivity databaseConnectivity,
            SensorServiceClient sensorServiceClient,
            SensorSimulatorConfig simulatorConfig
    ) {
        configureLoggingConfig();
        this.databaseConnectivity = databaseConnectivity;

        var sensorDao = new SensorDao(databaseConnectivity);

        this.sensorSimulatorService = new SensorSimulatorService(
                sensorDao,
                sensorServiceClient,
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

    private void configureLoggingConfig() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        File configFile = new File("/function/code/logback.xml");
        if (configFile.exists()) {
            try {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                context.reset();
                configurator.doConfigure(configFile);
            } catch (JoranException je) {
                System.out.println(je.getMessage());
            }
        }
    }
}
