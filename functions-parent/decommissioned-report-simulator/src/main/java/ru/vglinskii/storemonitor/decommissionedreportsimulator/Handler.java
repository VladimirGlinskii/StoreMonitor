package ru.vglinskii.storemonitor.decommissionedreportsimulator;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.amazonaws.auth.BasicAWSCredentials;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.serviceclient.StorageServiceClient;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.config.ApplicationProperties;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.dao.DecommissionedReportDao;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.dao.StoreDao;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.service.CommodityService;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.service.DecommissionedReportGeneratorService;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.service.DecommissionedReportService;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.TriggerRequestDto;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<TriggerRequestDto, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private DatabaseConnectivity databaseConnectivity;
    private DecommissionedReportService decommissionedReportService;

    public Handler() {
        var properties = new ApplicationProperties();
        init(
                DatabaseConnectivityFactory.create(properties),
                new StorageServiceClient(
                        new BasicAWSCredentials(
                                properties.getSaAccessKey(),
                                properties.getSaSecretKey()
                        ),
                        properties.getBucketName()
                ),
                new CommodityService(properties.getMaxCommoditiesForDecommissionCount())
        );
    }

    public Handler(
            DatabaseConnectivity databaseConnectivity,
            StorageServiceClient storageServiceClient,
            CommodityService commodityService
    ) {
        init(databaseConnectivity, storageServiceClient, commodityService);
    }

    private void init(
            DatabaseConnectivity databaseConnectivity,
            StorageServiceClient storageServiceClient,
            CommodityService commodityService
    ) {
        configureLoggingConfig();
        this.databaseConnectivity = databaseConnectivity;

        var storeDao = new StoreDao(databaseConnectivity);
        var decommissionedReportDao = new DecommissionedReportDao(databaseConnectivity);
        var decommissionedReportGeneratorService = new DecommissionedReportGeneratorService();

        this.decommissionedReportService = new DecommissionedReportService(
                storeDao,
                decommissionedReportDao,
                storageServiceClient,
                commodityService,
                decommissionedReportGeneratorService
        );
    }

    @Override
    public String handle(TriggerRequestDto request, Context context) {
        try {
            LOGGER.info("Received request from trigger {}", request);

            decommissionedReportService.generateReports();

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
