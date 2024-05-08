package ru.vglinskii.storemonitor.updatesensorvalue.service;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonSensorValueDao;
import ru.vglinskii.storemonitor.functionscommon.model.SensorValue;
import ru.vglinskii.storemonitor.functionscommon.utils.BeanValidator;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.SensorValueDtoResponse;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.SensorsValuesDtoResponse;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.UpdateSensorValueDtoRequest;

public class SensorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SensorService.class);
    private CommonSensorValueDao sensorValueDao;
    private BeanValidator beanValidator;

    public SensorService(CommonSensorValueDao sensorValueDao, BeanValidator beanValidator) {
        this.sensorValueDao = sensorValueDao;
        this.beanValidator = beanValidator;
    }

    public SensorsValuesDtoResponse updateSensorsValues(List<UpdateSensorValueDtoRequest> values) {
        var updatedValuesDtos = new ArrayList<SensorValueDtoResponse>();
        for (var value : values) {
            try {
                beanValidator.validate(value);
                updatedValuesDtos.add(updateSensorValue(value));
            } catch (ConstraintViolationException e) {
                LOGGER.error("Sensor value didn't pass validation", e);
            }
        }
        return new SensorsValuesDtoResponse(updatedValuesDtos);
    }

    private SensorValueDtoResponse updateSensorValue(UpdateSensorValueDtoRequest request) {
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
        sensorValue = sensorValueDao.insert(sensorValue);

        return SensorValueDtoResponse.builder()
                .id(sensorValue.getId())
                .sensorId(sensorValue.getSensorId())
                .value(sensorValue.getValue())
                .datetime(sensorValue.getDatetime())
                .unit(sensorValue.getUnit())
                .build();
    }
}
