package ru.vglinskii.storemonitor.sensorsimulator.service;

import java.util.ArrayList;
import static org.apache.commons.rng.simple.RandomSource.KISS;
import org.apache.commons.statistics.distribution.ContinuousDistribution;
import org.apache.commons.statistics.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;
import ru.vglinskii.storemonitor.sensorsimulator.api.DevicesApi;
import ru.vglinskii.storemonitor.sensorsimulator.config.SensorSimulatorConfig;
import ru.vglinskii.storemonitor.sensorsimulator.dao.SensorDao;
import ru.vglinskii.storemonitor.sensorsimulator.dto.UpdateSensorValueDtoRequest;
import ru.vglinskii.storemonitor.sensorsimulator.dto.UpdateSensorsValuesDtoRequest;

public class SensorSimulatorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SensorSimulatorService.class);
    private SensorDao sensorDao;
    private DevicesApi devicesApi;
    private ContinuousDistribution.Sampler celsiusValueSampler;

    public SensorSimulatorService(
            SensorDao sensorDao,
            DevicesApi devicesApi,
            SensorSimulatorConfig config
    ) {
        this.sensorDao = sensorDao;
        this.devicesApi = devicesApi;
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

        LOGGER.info("Sending request to update sensors values");
        var response = devicesApi.updateSensorsValues(new UpdateSensorsValuesDtoRequest(updateRequestDtos));

        if (response.is2xxSuccessful()) {
            LOGGER.info("Update sensors values request sent");
        } else {
            LOGGER.error("Update sensors values request failed");
        }
    }
}
