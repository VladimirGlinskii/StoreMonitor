package ru.vglinskii.storemonitor.authfunction;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.authfunction.dao.EmployeeDao;
import ru.vglinskii.storemonitor.authfunction.dto.ResponseDto;
import ru.vglinskii.storemonitor.authfunction.service.AuthService;
import ru.vglinskii.storemonitor.functionscommon.config.ApplicationProperties;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<HttpRequestDto, ResponseDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private ApplicationProperties properties;
    private DatabaseConnectivity databaseConnectivity;
    private AuthService authService;

    public Handler() {
        this(new ApplicationProperties());
    }

    public Handler(ApplicationProperties properties) {
        this.properties = properties;
        var dbProps = new Properties();
        dbProps.setProperty("ssl", "true");
        dbProps.setProperty("user", properties.getDbUser());
        dbProps.setProperty("password", properties.getDbPassword());

        this.databaseConnectivity = new DatabaseConnectivity(properties.getDbUrl(), dbProps);

        var employeeDao = new EmployeeDao(databaseConnectivity);
        this.authService = new AuthService(employeeDao);
    }

    @Override
    public ResponseDto handle(HttpRequestDto request, Context context) {
        LOGGER.info("Received request {}", request);

        try {
            var secretKey = request.getHeaders().getOrDefault("X-Secret-Key", "");
            var storeId = Long.parseLong(request.getHeaders().getOrDefault("X-Store-Id", ""));
            var authorizationContext = authService.authorize(secretKey, storeId);

            return ResponseDto.builder()
                    .isAuthorized(authorizationContext != null)
                    .context(authorizationContext)
                    .build();
        } catch (Throwable e) {
            LOGGER.error("Unhandled exception", e);

            throw e;
        }
    }
}
