package ru.vglinskii.storemonitor.sensorsimulator.service;

import java.util.ArrayList;
import static org.apache.commons.rng.simple.RandomSource.KISS;
import org.apache.commons.statistics.distribution.ContinuousDistribution;
import org.apache.commons.statistics.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;
import ru.vglinskii.storemonitor.sensorsimulator.serviceclient.SensorServiceClient;
import ru.vglinskii.storemonitor.sensorsimulator.config.SensorSimulatorConfig;
import ru.vglinskii.storemonitor.sensorsimulator.dao.SensorDao;
import ru.vglinskii.storemonitor.sensorsimulator.dto.UpdateSensorValueDtoRequest;
import ru.vglinskii.storemonitor.sensorsimulator.dto.UpdateSensorsValuesDtoRequest;

public class SensorSimulatorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SensorSimulatorService.class);
    private SensorDao sensorDao;
    private SensorServiceClient sensorServiceClient;
    private ContinuousDistribution.Sampler celsiusValueSampler;

    public SensorSimulatorService(
            SensorDao sensorDao,
            SensorServiceClient sensorServiceClient,
            SensorSimulatorConfig config
    ) {
        this.sensorDao = sensorDao;
        this.sensorServiceClient = sensorServiceClient;
        this.celsiusValueSampler = NormalDistribution
                .of(
                        config.getSensorValueCelsiusMean(),
                        config.getSensorValueCelsiusStandardDeviation()
                )
                .createSampler(KISS.create());
    }

    public void updateSensorsValues() {
        var sensors = sensorDao.findAll();

        var updateRequestDtos = new ArrayList<UpdateSensorValueDtoRequest>();
        for (var sensor : sensors) {
            updateRequestDtos.add(
                    new UpdateSensorValueDtoRequest(
                            sensor.getId(),
                            (float) celsiusValueSampler.sample(),
                            SensorUnit.CELSIUS
                    )
            );
        }

        LOGGER.info("Updating sensors' values...");
        var isSuccess = sensorServiceClient.updateSensorsValues(new UpdateSensorsValuesDtoRequest(updateRequestDtos));

        if (isSuccess) {
            LOGGER.info("Sensors' values successfully updated");
        } else {
            LOGGER.error("Failed to update sensors' values");
        }
    }
}
