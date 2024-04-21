package ru.vglinskii.storemonitor.decommissionedreportsimulator;

import com.amazonaws.auth.BasicAWSCredentials;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.api.StorageApi;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.config.ApplicationProperties;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.dao.DecommissionedReportDao;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.dao.StoreDao;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.service.CommodityService;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.service.DecommissionedReportGeneratorService;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.service.DecommissionedReportService;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.dto.TriggerRequestDto;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<TriggerRequestDto, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private ApplicationProperties properties;
    private DatabaseConnectivity databaseConnectivity;
    private DecommissionedReportService decommissionedReportService;

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

        var storeDao = new StoreDao(databaseConnectivity);
        var decommissionedReportDao = new DecommissionedReportDao(databaseConnectivity);
        var storageApi = new StorageApi(
                new BasicAWSCredentials(
                        properties.getSaAccessKey(),
                        properties.getSaSecretKey()
                ),
                properties.getBucketName()
        );
        var commodityService = new CommodityService(properties.getMaxCommoditiesForDecommissionCount());
        var decommissionedReportGeneratorService = new DecommissionedReportGeneratorService();
        this.decommissionedReportService = new DecommissionedReportService(
                storeDao,
                decommissionedReportDao,
                storageApi,
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
}
