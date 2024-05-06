package ru.vglinskii.storemonitor.decommissionedreportsimulator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.serviceclient.StorageServiceClient;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.dao.DecommissionedReportDao;
import ru.vglinskii.storemonitor.decommissionedreportsimulator.service.CommodityService;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonStoreDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.model.Store;
import ru.vglinskii.storemonitor.functionscommon.utils.TestContext;

@ExtendWith(MockitoExtension.class)
public class DecommissionedReportSimulatorIT {
    private Handler handler;
    private StorageServiceClient storageServiceClient;

    private CommonStoreDao storeDao;
    private DecommissionedReportDao decommissionedReportDao;

    private Store store1;

    public DecommissionedReportSimulatorIT() {
        var databaseConnectivity = DatabaseConnectivityFactory.create(
                "jdbc:mysql://localhost:3306/store-monitor-test",
                "root",
                "root"
        );
        this.storageServiceClient = Mockito.mock(StorageServiceClient.class);
        this.handler = new Handler(
                databaseConnectivity,
                storageServiceClient,
                new CommodityService(10)
        );
        this.storeDao = new CommonStoreDao(databaseConnectivity);
        this.decommissionedReportDao = new DecommissionedReportDao(databaseConnectivity);
    }

    @BeforeEach
    void init() {
        decommissionedReportDao.deleteAll();
        storeDao.deleteAll();

        store1 = storeDao.insert(Store.builder().location("Location 1").build());
    }

    @Test
    void handle_shouldSaveReportToStorageAndDatabase() {
        var dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                .withZone(ZoneId.systemDefault());
        var now = Instant.now();
        var storageKeyCaptor = ArgumentCaptor.forClass(String.class);
        var reportContentCaptor = ArgumentCaptor.forClass(byte[].class);
        var expectedStorageKey = String.join(
                "/",
                store1.getId().toString(),
                dateFormatter.format(now),
                "report.xlsx"
        );

        triggerSimulation();

        Mockito.verify(storageServiceClient)
                .uploadObject(
                        storageKeyCaptor.capture(),
                        reportContentCaptor.capture()
                );

        Assertions.assertEquals(expectedStorageKey, storageKeyCaptor.getValue());
        Assertions.assertTrue(reportContentCaptor.getValue().length > 0);

        var reports = decommissionedReportDao.findAll();
        Assertions.assertEquals(1, reports.size());

        var savedReport = reports.get(0);
        Assertions.assertEquals(expectedStorageKey, savedReport.getLink());
        Assertions.assertEquals(store1.getId(), savedReport.getStoreId());
    }

    private void triggerSimulation() {
        handler.handle(null, new TestContext());
    }
}
