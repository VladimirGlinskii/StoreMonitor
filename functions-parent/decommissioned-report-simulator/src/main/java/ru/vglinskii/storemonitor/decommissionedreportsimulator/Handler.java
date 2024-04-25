package ru.vglinskii.storemonitor.decommissionedreportsimulator;

import com.amazonaws.auth.BasicAWSCredentials;
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
                new StorageApi(
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
            StorageApi storageApi,
            CommodityService commodityService
    ) {
        init(databaseConnectivity, storageApi, commodityService);
    }

    private void init(
            DatabaseConnectivity databaseConnectivity,
            StorageApi storageApi,
            CommodityService commodityService
    ) {
        this.databaseConnectivity = databaseConnectivity;

        var storeDao = new StoreDao(databaseConnectivity);
        var decommissionedReportDao = new DecommissionedReportDao(databaseConnectivity);
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
