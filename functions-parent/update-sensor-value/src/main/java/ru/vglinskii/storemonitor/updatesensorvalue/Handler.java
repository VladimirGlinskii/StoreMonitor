package ru.vglinskii.storemonitor.updatesensorvalue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonSensorValueDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpResponseDto;
import ru.vglinskii.storemonitor.functionscommon.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.functionscommon.exception.ErrorCode;
import ru.vglinskii.storemonitor.functionscommon.exception.GlobalExceptionHandler;
import ru.vglinskii.storemonitor.functionscommon.utils.BeanValidator;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import ru.vglinskii.storemonitor.updatesensorvalue.dto.UpdateSensorsValuesDtoRequest;
import ru.vglinskii.storemonitor.updatesensorvalue.service.SensorService;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<HttpRequestDto, HttpResponseDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private DatabaseConnectivity databaseConnectivity;
    private ObjectMapper objectMapper;
    private GlobalExceptionHandler globalExceptionHandler;
    private SensorService sensorService;

    public Handler() {
        this(
                DatabaseConnectivityFactory.create(
                        new CommonApplicationProperties()
                )
        );
    }

    public Handler(DatabaseConnectivity databaseConnectivity) {
        this.objectMapper = new AppObjectMapper();
        this.globalExceptionHandler = new GlobalExceptionHandler(objectMapper);
        this.databaseConnectivity = databaseConnectivity;

        var sensorValueDao = new CommonSensorValueDao(databaseConnectivity);
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

                return new HttpResponseDto(
                        HttpStatus.SC_OK,
                        objectMapper.writeValueAsString(sensorService.updateSensorsValues(valuesDto)),
                        Map.ofEntries(Map.entry(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
                );
            } catch (JsonProcessingException e) {
                LOGGER.error("Invalid request", e);
                throw new AppRuntimeException(ErrorCode.INVALID_REQUEST);
            }
        });
    }
}
