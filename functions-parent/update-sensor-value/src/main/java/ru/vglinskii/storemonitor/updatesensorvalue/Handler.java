package ru.vglinskii.storemonitor.updatesensorvalue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.config.ApplicationProperties;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpResponseDto;
import ru.vglinskii.storemonitor.functionscommon.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.functionscommon.exception.ErrorCode;
import ru.vglinskii.storemonitor.functionscommon.exception.GlobalExceptionHandler;
import ru.vglinskii.storemonitor.functionscommon.utils.BeanValidator;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import ru.vglinskii.storemonitor.updatesensorvalue.dao.SensorValueDao;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.UpdateSensorsValuesDtoRequest;
import ru.vglinskii.storemonitor.updatesensorvalue.service.SensorService;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<HttpRequestDto, HttpResponseDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private ApplicationProperties properties;
    private DatabaseConnectivity databaseConnectivity;
    private ObjectMapper objectMapper;
    private GlobalExceptionHandler globalExceptionHandler;
    private SensorService sensorService;

    public Handler() {
        this(new ApplicationProperties());
    }

    public Handler(ApplicationProperties properties) {
        this.properties = properties;
        this.objectMapper = new AppObjectMapper();
        this.globalExceptionHandler = new GlobalExceptionHandler(objectMapper);

        var dbProps = new Properties();
        dbProps.setProperty("ssl", "true");
        dbProps.setProperty("user", properties.getDbUser());
        dbProps.setProperty("password", properties.getDbPassword());
        this.databaseConnectivity = new DatabaseConnectivity(properties.getDbUrl(), dbProps);

        var sensorValueDao = new SensorValueDao(databaseConnectivity);
        var beanValidator = new BeanValidator();
        this.sensorService = new SensorService(sensorValueDao, beanValidator);
    }

    @Override
    public HttpResponseDto handle(HttpRequestDto request, Context context) {
        LOGGER.info("Received request {}", request);
        return globalExceptionHandler.handle(() -> {
            try {
                var valuesDto = Optional
                        .ofNullable(
                                objectMapper.readValue(request.getBody(), UpdateSensorsValuesDtoRequest.class)
                                        .getValues()
                        )
                        .orElse(List.of());

                sensorService.updateSensorsValues(valuesDto);

                return new HttpResponseDto(HttpStatus.SC_OK, "", Map.of());
            } catch (JsonProcessingException e) {
                LOGGER.error("Invalid request", e);
                throw new AppRuntimeException(ErrorCode.INVALID_REQUEST);
            }
        });
    }
}
