package ru.vglinskii.storemonitor.authfunction;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vglinskii.storemonitor.authfunction.dto.ResponseDto;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonEmployeeDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonStoreDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.model.Employee;
import ru.vglinskii.storemonitor.functionscommon.model.Store;
import ru.vglinskii.storemonitor.functionscommon.utils.TestContext;

public class AuthFunctionIT {
    private Handler handler;
    private CommonStoreDao storeDao;
    private CommonEmployeeDao employeeDao;
    private Store store1;
    private Employee directorInStore1;

    public AuthFunctionIT() {
        var databaseConnectivity = DatabaseConnectivityFactory.create(
                "jdbc:mysql://localhost:3306/store-monitor-test",
                "root",
                "root"
        );
        this.handler = new Handler(databaseConnectivity);
        this.storeDao = new CommonStoreDao(databaseConnectivity);
        this.employeeDao = new CommonEmployeeDao(databaseConnectivity);
    }

    @BeforeEach
    void init() {
        employeeDao.deleteAll();
        storeDao.deleteAll();

        store1 = storeDao.insert(
                Store.builder()
                        .location("Location 1")
                        .build()
        );

        directorInStore1 = employeeDao.insert(
                Employee.builder()
                        .firstName("Firstname1")
                        .lastName("Lastname1")
                        .type(EmployeeType.DIRECTOR)
                        .storeId(store1.getId())
                        .secret("secret1")
                        .build()
        );
    }

    @Test
    void whenSecretValid_handle_shouldReturnOkResponseWithContext() {
        var expectedResponse = ResponseDto.builder()
                .isAuthorized(true)
                .context(new AuthorizationContextDto(
                        store1.getId(),
                        directorInStore1.getId()
                ))
                .build();
        var response = sendRequest(directorInStore1.getSecret());
        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    void whenSecretInvalid_handle_shouldReturnUnauthorizedResponseWithoutContext() {
        var expectedResponse = ResponseDto.builder()
                .isAuthorized(false)
                .context(null)
                .build();
        var response = sendRequest("wrongSecret");
        Assertions.assertEquals(expectedResponse, response);
    }

    private ResponseDto sendRequest(String secret) {
        var request = HttpRequestDto.builder()
                .headers(Map.of("X-Secret-Key", secret))
                .build();

        return handler.handle(request, new TestContext());
    }
}
