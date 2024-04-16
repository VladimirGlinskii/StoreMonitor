package ru.vglinskii.storemonitor.updatesensorvalue.service;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.utils.BeanValidator;
import ru.vglinskii.storemonitor.updatesensorvalue.dao.SensorValueDao;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.UpdateSensorValueDtoRequest;
import ru.vglinskii.storemonitor.updatesensorvalue.model.SensorValue;

public class SensorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SensorService.class);
    private SensorValueDao sensorValueDao;
    private BeanValidator beanValidator;

    public SensorService(SensorValueDao sensorValueDao, BeanValidator beanValidator) {
        this.sensorValueDao = sensorValueDao;
        this.beanValidator = beanValidator;
    }

    public void updateSensorsValues(List<UpdateSensorValueDtoRequest> values) {
        for (var value : values) {
            try {
                beanValidator.validate(value);
                updateSensorValue(value);
            } catch (ConstraintViolationException e) {
                LOGGER.error("Sensor value didn't pass validation", e);
            }
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
