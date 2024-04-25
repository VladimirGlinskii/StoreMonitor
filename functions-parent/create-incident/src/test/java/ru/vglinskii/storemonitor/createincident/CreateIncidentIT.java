package ru.vglinskii.storemonitor.createincident;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;
import ru.vglinskii.storemonitor.common.enums.IncidentType;
import ru.vglinskii.storemonitor.createincident.dto.CreateIncidentDtoRequest;
import ru.vglinskii.storemonitor.createincident.dto.IncidentDtoResponse;
import ru.vglinskii.storemonitor.createincident.utils.ValidationErrorMessages;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonEmployeeDao;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonStoreDao;
import ru.vglinskii.storemonitor.functionscommon.database.DatabaseConnectivityFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.ErrorDtoResponse;
import ru.vglinskii.storemonitor.functionscommon.dto.ErrorsDtoResponse;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpResponseDto;
import ru.vglinskii.storemonitor.functionscommon.dto.RequestContext;
import ru.vglinskii.storemonitor.functionscommon.exception.ErrorCode;
import ru.vglinskii.storemonitor.functionscommon.model.Employee;
import ru.vglinskii.storemonitor.functionscommon.model.Store;
import ru.vglinskii.storemonitor.functionscommon.utils.TestContext;
import ru.vglinskii.storemonitor.functionscommon.utils.serialization.AppObjectMapper;

public class CreateIncidentIT {
    private Handler handler;
    private ObjectMapper objectMapper;
    private CommonStoreDao storeDao;
    private CommonEmployeeDao employeeDao;
    private Store store1;
    private Employee directorInStore1;

    public CreateIncidentIT() {
        var databaseConnectivity = DatabaseConnectivityFactory.create(
                "jdbc:mysql://localhost:3306/store-monitor-test",
                "root",
                "root"
        );
        this.handler = new Handler(databaseConnectivity);
        this.objectMapper = new AppObjectMapper();
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
    void whenValid_handle_shouldReturnSuccessResponse() throws Exception {
        var requestBody = CreateIncidentDtoRequest.builder()
                .datetime(Instant.now())
                .description("Some Description")
                .eventType(IncidentType.INJURY)
                .calledAmbulance(true)
                .calledFireDepartment(true)
                .calledGasService(true)
                .calledPolice(true)
                .build();
        var expectedResponseBody = IncidentDtoResponse.builder()
                .storeId(store1.getId())
                .eventType(requestBody.getEventType())
                .datetime(requestBody.getDatetime())
                .description(requestBody.getDescription())
                .calledAmbulance(requestBody.isCalledAmbulance())
                .calledFireDepartment(requestBody.isCalledFireDepartment())
                .calledGasService(requestBody.isCalledGasService())
                .calledPolice(requestBody.isCalledPolice())
                .build();
        var response = sendRequest(
                objectMapper.writeValueAsString(requestBody),
                new AuthorizationContextDto(store1.getId(), directorInStore1.getId())
        );
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        var responseBody = objectMapper.readValue(response.getBody(), IncidentDtoResponse.class);

        Assertions.assertTrue(responseBody.getId() > 0);

        expectedResponseBody.setId(responseBody.getId());

        Assertions.assertEquals(expectedResponseBody, responseBody);
    }

    @Test
    void whenInvalid_handle_shouldReturnErrorResponse() throws Exception {
        var requestBody = CreateIncidentDtoRequest.builder().build();
        var expectedErrors = List.of(
                new ErrorDtoResponse(ErrorCode.FIELD_NOT_VALID, ValidationErrorMessages.REQUIRED_FIELD, "datetime"),
                new ErrorDtoResponse(ErrorCode.FIELD_NOT_VALID, ValidationErrorMessages.REQUIRED_FIELD, "description"),
                new ErrorDtoResponse(ErrorCode.FIELD_NOT_VALID, ValidationErrorMessages.REQUIRED_FIELD, "eventType")
        );
        var response = sendRequest(
                objectMapper.writeValueAsString(requestBody),
                new AuthorizationContextDto(store1.getId(), directorInStore1.getId())
        );
        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCode());

        var errors = objectMapper.readValue(response.getBody(), ErrorsDtoResponse.class)
                .getErrors();

        Assertions.assertEquals(expectedErrors.size(), errors.size());
        Assertions.assertTrue(expectedErrors.containsAll(errors));
    }

    private HttpResponseDto sendRequest(String requestBody, AuthorizationContextDto authorizationContext) {
        var request = HttpRequestDto.builder()
                .body(requestBody)
                .requestContext(new RequestContext(authorizationContext))
                .build();

        return handler.handle(request, new TestContext());
    }
}
