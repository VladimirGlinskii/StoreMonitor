package ru.vglinskii.storemonitor.incidentsreport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.config.ApplicationProperties;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpResponseDto;
import ru.vglinskii.storemonitor.functionscommon.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.functionscommon.exception.ErrorCode;
import ru.vglinskii.storemonitor.functionscommon.exception.GlobalExceptionHandler;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import ru.vglinskii.storemonitor.incidentsreport.dao.IncidentDao;
import ru.vglinskii.storemonitor.incidentsreport.service.IncidentService;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<HttpRequestDto, HttpResponseDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private ApplicationProperties properties;
    private DatabaseConnectivity databaseConnectivity;
    private ObjectMapper objectMapper;
    private GlobalExceptionHandler globalExceptionHandler;
    private IncidentService incidentService;

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

        var incidentDao = new IncidentDao(databaseConnectivity);
        this.incidentService = new IncidentService(incidentDao);
    }

    @Override
    public HttpResponseDto handle(HttpRequestDto request, Context context) {
        LOGGER.info("Received request {}", request);
        return globalExceptionHandler.handle(() -> {
            try {
                var authContext = request.getRequestContext().getAuthorizer();
                var from = Instant.parse(request.getQueryStringParameters().get("from"));
                var to = Instant.parse(request.getQueryStringParameters().get("to"));

                var reportDto = incidentService.getIncidentsReport(authContext.getStoreId(), from, to);

                return new HttpResponseDto(
                        HttpStatus.SC_OK,
                        objectMapper.writeValueAsString(reportDto),
                        Map.of()
                );
            } catch (JsonProcessingException e) {
                LOGGER.error("Invalid request", e);
                throw new AppRuntimeException(ErrorCode.INVALID_REQUEST);
            }
        });
    }
}