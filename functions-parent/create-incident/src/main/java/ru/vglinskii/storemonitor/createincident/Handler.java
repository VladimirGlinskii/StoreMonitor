package ru.vglinskii.storemonitor.createincident;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.createincident.dto.CreateIncidentDtoRequest;
import ru.vglinskii.storemonitor.createincident.service.IncidentService;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonIncidentDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpResponseDto;
import ru.vglinskii.storemonitor.functionscommon.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.functionscommon.exception.ErrorCode;
import ru.vglinskii.storemonitor.functionscommon.exception.GlobalExceptionHandler;
import ru.vglinskii.storemonitor.functionscommon.utils.BeanValidator;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<HttpRequestDto, HttpResponseDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private DatabaseConnectivity databaseConnectivity;
    private ObjectMapper objectMapper;
    private GlobalExceptionHandler globalExceptionHandler;
    private BeanValidator beanValidator;
    private IncidentService incidentService;

    public Handler() {
        this(DatabaseConnectivityFactory.create(
                new CommonApplicationProperties()
        ));
    }

    public Handler(DatabaseConnectivity databaseConnectivity) {
        this.objectMapper = new AppObjectMapper();
        this.globalExceptionHandler = new GlobalExceptionHandler(objectMapper);
        this.beanValidator = new BeanValidator();
        this.databaseConnectivity = databaseConnectivity;
        var incidentDao = new CommonIncidentDao(databaseConnectivity);
        this.incidentService = new IncidentService(incidentDao);
    }

    @Override
    public HttpResponseDto handle(HttpRequestDto request, Context context) {
        LOGGER.info("Received request {}", request);
        return globalExceptionHandler.handle(() -> {
            try {
                var authContext = request.getRequestContext().getAuthorizer();
                var requestBody = objectMapper.readValue(request.getBody(), CreateIncidentDtoRequest.class);
                beanValidator.validate(requestBody);

                return new HttpResponseDto(
                        HttpStatus.SC_OK,
                        objectMapper.writeValueAsString(
                                incidentService.createIncident(requestBody, authContext.getStoreId())
                        ),
                        Map.of()
                );
            } catch (JsonProcessingException e) {
                LOGGER.error("Invalid request", e);
                throw new AppRuntimeException(ErrorCode.INVALID_REQUEST);
            }
        });
    }
}
