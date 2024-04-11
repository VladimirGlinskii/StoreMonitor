package ru.vglinskii.storemonitor.decommissionedreportsimulator.service;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.api.StorageApi;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.api.StorageException;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.dao.DecommissionedReportDao;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.dao.StoreDao;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.model.DecommissionedReport;
import ru.vglinskii.storemonitor.functionscommon.dao.DataAccessException;

public class DecommissionedReportService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DecommissionedReportService.class);
    private StoreDao storeDao;
    private DecommissionedReportDao reportDao;
    private StorageApi storageApi;
    private CommodityService commodityService;
    private DecommissionedReportGeneratorService reportGeneratorService;
    private DateTimeFormatter objectDateFormatter;

    public DecommissionedReportService(
            StoreDao storeDao,
            DecommissionedReportDao reportDao,
            StorageApi storageApi,
            CommodityService commodityService,
            DecommissionedReportGeneratorService reportGeneratorService
    ) {
        this.storeDao = storeDao;
        this.reportDao = reportDao;
        this.storageApi = storageApi;
        this.commodityService = commodityService;
        this.reportGeneratorService = reportGeneratorService;
        this.objectDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                .withZone(ZoneId.systemDefault());
    }

    public void generateReports() {
        var stores = storeDao.findAll();
        var now = Instant.now();

        for (var store : stores) {
            var objectKey = generateObjectKey(store.getId(), now);
            var reportEntity = DecommissionedReport.builder()
                    .storeId(store.getId())
                    .link(objectKey)
                    .createdAt(now)
                    .build();
            var commodities = commodityService.getCommoditiesForDecommission();
            var reportFile = reportGeneratorService.generateReportContent(reportEntity, commodities);
            uploadReportObject(reportEntity, reportFile);
            saveReportEntity(reportEntity);
        }
    }

    private void uploadReportObject(DecommissionedReport reportEntity, byte[] reportContent) {
        try {
            LOGGER.info("Uploading report object {} for store {}", reportEntity.getLink(), reportEntity.getStoreId());
            storageApi.uploadObject(reportEntity.getLink(), reportContent);
            LOGGER.info("Uploaded report object {} for store {}", reportEntity.getLink(), reportEntity.getStoreId());
        } catch (StorageException e) {
            LOGGER.error("Failed to upload report object {} for store {}", reportEntity.getLink(), reportEntity.getStoreId(), e);

            throw e;
        }
    }

    private void saveReportEntity(DecommissionedReport reportEntity) {
        try {
            LOGGER.info("Saving report entity {} for store {} to the DB", reportEntity.getLink(), reportEntity.getStoreId());
            reportDao.create(reportEntity);
            LOGGER.info("Saved report entity {} for store {} to the DB", reportEntity.getLink(), reportEntity.getStoreId());
        } catch (DataAccessException e) {
            LOGGER.error("Failed to save report object {} for store {} to the DB", reportEntity.getLink(), reportEntity.getStoreId(), e);

            throw e;
        }
    }

    private String generateObjectKey(long storeId, Instant date) {
        return String.join("/", List.of(
                String.valueOf(storeId),
                objectDateFormatter.format(date),
                "report.xlsx"
        ));
    }
}
