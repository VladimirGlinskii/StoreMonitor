package ru.vglinskii.storemonitor.updatesensorvalue.service;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.updatesensorvalue.dao.SensorValueDao;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.UpdateSensorValueDtoRequest;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.UpdateSensorsValuesDtoRequest;
import ru.vglinskii.storemonitor.updatesensorvalue.model.SensorValue;

public class SensorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SensorService.class);
    private SensorValueDao sensorValueDao;

    public SensorService(SensorValueDao sensorValueDao) {
        this.sensorValueDao = sensorValueDao;
    }

    public void updateSensorsValues(UpdateSensorsValuesDtoRequest request) {
        for (var value : request.getValues()) {
            updateSensorValue(value);
        }
    }

    private void updateSensorValue(UpdateSensorValueDtoRequest request) {
        LOGGER.info(
                "Updating sensor {} value to {} {}",
                request.getSensorId(),
                request.getValue(),
                request.getUnit()
        );
        var sensorValue = SensorValue.builder()
                .sensorId(request.getSensorId())
                .value(request.getValue())
                .unit(request.getUnit())
                .datetime(Instant.now())
                .build();
        sensorValueDao.create(sensorValue);
    }
}
