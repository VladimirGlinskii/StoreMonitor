package ru.vglinskii.storemonitor.incidentsreport;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Instant;
import java.util.Map;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
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
    private DatabaseConnectivity databaseConnectivity;
    private ObjectMapper objectMapper;
    private GlobalExceptionHandler globalExceptionHandler;
    private IncidentService incidentService;

    public Handler() {
        this(
                DatabaseConnectivityFactory.create(
                        new CommonApplicationProperties()
                )
        );
    }

    public Handler(DatabaseConnectivity databaseConnectivity) {
        configureLoggingConfig();
        this.objectMapper = new AppObjectMapper();
        this.globalExceptionHandler = new GlobalExceptionHandler(objectMapper);
        this.databaseConnectivity = databaseConnectivity;

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
                        Map.ofEntries(Map.entry(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
                );
            } catch (JsonProcessingException e) {
                LOGGER.error("Invalid request", e);
                throw new AppRuntimeException(ErrorCode.INVALID_REQUEST);
            }
        });
    }

    private void configureLoggingConfig() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        File configFile = new File("/function/code/logback.xml");
        if (configFile.exists()) {
            try {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                context.reset();
                configurator.doConfigure(configFile);
            } catch (JoranException je) {
                System.out.println(je.getMessage());
            }
        }
    }
}
