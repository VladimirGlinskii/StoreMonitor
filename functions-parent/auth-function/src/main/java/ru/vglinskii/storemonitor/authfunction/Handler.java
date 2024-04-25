package ru.vglinskii.storemonitor.authfunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.authfunction.dao.EmployeeDao;
import ru.vglinskii.storemonitor.authfunction.dto.ResponseDto;
import ru.vglinskii.storemonitor.authfunction.service.AuthService;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivity;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import yandex.cloud.sdk.functions.Context;
import yandex.cloud.sdk.functions.YcFunction;

public class Handler implements YcFunction<HttpRequestDto, ResponseDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Handler.class);
    private DatabaseConnectivity databaseConnectivity;
    private AuthService authService;

    public Handler() {
        this(DatabaseConnectivityFactory.create(
                new CommonApplicationProperties()
        ));
    }

    public Handler(DatabaseConnectivity databaseConnectivity) {
        this.databaseConnectivity = databaseConnectivity;
        var employeeDao = new EmployeeDao(databaseConnectivity);
        this.authService = new AuthService(employeeDao);
    }

    @Override
    public ResponseDto handle(HttpRequestDto request, Context context) {
        LOGGER.info("Received request {}", request);

        try {
            var secretKey = request.getHeaders().getOrDefault("X-Secret-Key", "");
            var authorizationContext = authService.authorize(secretKey);

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
