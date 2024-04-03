package ru.vglinskii.storemonitor.authfunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.authfunction.dto.ResponseDto;
import ru.vglinskii.storemonitor.authfunction.service.AuthService;
import ru.vglinskii.storemonitor.functionscommon.config.ApplicationProperties;
import ru.vglinskii.storemonitor.functionscommon.dao.EmployeeDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.dto.RequestDto;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<RequestDto, ResponseDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private ObjectMapper objectMapper;
    private ApplicationProperties properties;
    private DatabaseConnectivity databaseConnectivity;
    private AuthService authService;

    public Handler() {
        this(new ApplicationProperties(), new DatabaseConnectivity());
    }

    public Handler(ApplicationProperties properties, DatabaseConnectivity databaseConnectivity) {
        this.objectMapper = new AppObjectMapper();
        this.properties = properties;
        this.databaseConnectivity = databaseConnectivity;
        var employeeDao = new EmployeeDao(databaseConnectivity);
        this.authService = new AuthService(employeeDao);
    }

    @Override
    public ResponseDto handle(RequestDto request, Context context) {
        LOGGER.info("Received request {}", request);

        try {
            if (databaseConnectivity.getConnection() == null) {
                initDatabaseConnection();
            }
            var secretKey = request.getHeaders().getOrDefault("X-Secret-Key", "");
            var storeId = Long.parseLong(request.getHeaders().getOrDefault("X-Store-Id", ""));
            var authorizationContext = authService.authorize(secretKey, storeId);

            return ResponseDto.builder()
                    .isAuthorized(authorizationContext != null)
                    .context(authorizationContext)
                    .build();
        } catch (Throwable e) {
            LOGGER.error("Unhandled exception", e);

            return ResponseDto.builder()
                    .isAuthorized(false)
                    .build();
        }
    }

    private void initDatabaseConnection() {
        var dbProps = new Properties();
        dbProps.setProperty("ssl", "true");
        dbProps.setProperty("user", properties.getDbUser());
        dbProps.setProperty("password", properties.getDbPassword());

        databaseConnectivity.initConnection(properties.getDbUrl(), dbProps);
    }
}
