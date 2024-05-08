package ru.vglinskii.storemonitor.baseapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import ru.vglinskii.storemonitor.baseapi.dto.ErrorDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.ErrorsDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterStatusDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersStatusesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegistersWorkSummaryDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.exception.ErrorCode;
import ru.vglinskii.storemonitor.baseapi.service.CashRegisterService;
import ru.vglinskii.storemonitor.baseapi.utils.CashRegisterTestApi;
import ru.vglinskii.storemonitor.baseapi.utils.ValidationErrorMessages;

@WebMvcTest(CashRegisterController.class)
@Import({CashRegisterTestApi.class, ControllerTestConfiguration.class})
public class CashRegisterControllerTest extends ControllerTestBase {
    @MockBean
    private CashRegisterService cashRegisterService;
    @Autowired
    private CashRegisterTestApi cashRegisterTestApi;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenValid_create_shouldSuccess() throws Exception {
        var request = CreateCashRegisterDtoRequest.builder()
                .inventoryNumber("00000001")
                .build();
        var expectedResponse = CashRegisterDtoResponse.builder()
                .id(1)
                .opened(false)
                .inventoryNumber(request.getInventoryNumber())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Mockito.when(cashRegisterService.create(request))
                .thenReturn(expectedResponse);

        var response = objectMapper.readValue(
                cashRegisterTestApi.createCashRegister(testDirector, request)
                        .andExpect(status().is2xxSuccessful())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                CashRegisterDtoResponse.class
        );

        Assertions.assertEquals(expectedResponse, response);
    }

    private static Stream<Arguments> getInvalidCreateCashRegisterRequests() {
        return Stream.of(
                Arguments.of(
                        CreateCashRegisterDtoRequest.builder().build(),
                        List.of(
                                new ErrorDtoResponse(
                                        ErrorCode.FIELD_NOT_VALID,
                                        ValidationErrorMessages.REQUIRED_FIELD,
                                        "inventoryNumber"
                                )
                        )
                ),
                Arguments.of(
                        CreateCashRegisterDtoRequest.builder()
                                .inventoryNumber("")
                                .build(),
                        List.of(
                                new ErrorDtoResponse(
                                        ErrorCode.FIELD_NOT_VALID,
                                        ValidationErrorMessages.REQUIRED_FIELD,
                                        "inventoryNumber"
                                )
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidCreateCashRegisterRequests")
    void whenInvalid_create_shouldReturnError(
            CreateCashRegisterDtoRequest request,
            List<ErrorDtoResponse> expectedErrors
    ) throws Exception {
        var response = objectMapper.readValue(
                cashRegisterTestApi.createCashRegister(testDirector, request)
                        .andExpect(status().isBadRequest())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                ErrorsDtoResponse.class
        );

        Mockito.verify(cashRegisterService, Mockito.never()).create(Mockito.any());

        Assertions.assertEquals(response.getErrors().size(), expectedErrors.size());
        Assertions.assertTrue(response.getErrors().containsAll(expectedErrors));
    }

    @Test
    void delete_shouldSuccess() throws Exception {
        var cashRegisterId = 10;

        cashRegisterTestApi.deleteCashRegister(testDirector, cashRegisterId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        Mockito.verify(cashRegisterService, Mockito.times(1))
                .delete(cashRegisterId);
    }

    @Test
    void whenValid_openSession_shouldSuccess() throws Exception {
        var registerId = 10L;

        cashRegisterTestApi.openCashRegisterSession(testCashier, registerId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        Mockito.verify(cashRegisterService, Mockito.times(1))
                .openSession(registerId);
    }

    @Test
    void whenValid_closeSession_shouldSuccess() throws Exception {
        var registerId = 10L;

        cashRegisterTestApi.closeCashRegisterSession(testCashier, registerId)
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        Mockito.verify(cashRegisterService, Mockito.times(1))
                .closeSession(registerId);
    }

    @Test
    void getStatuses_shouldSuccess() throws Exception {
        var expectedResponse = new CashRegistersStatusesDtoResponse(
                List.of(
                        CashRegisterStatusDtoResponse.builder()
                                .id(10)
                                .inventoryNumber("00000010")
                                .opened(true)
                                .build()
                )
        );

        Mockito.when(cashRegisterService.getStatuses())
                .thenReturn(expectedResponse);

        var response = objectMapper.readValue(
                cashRegisterTestApi.getCashRegistersStatuses(testDirector)
                        .andExpect(status().is2xxSuccessful())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                CashRegistersStatusesDtoResponse.class
        );

        Assertions.assertEquals(expectedResponse, response);
    }

    @Test
    void getWorkSummary_shouldSuccess() throws Exception {
        var from = Instant.now().minus(5, ChronoUnit.HOURS);
        var to = Instant.now();
        var expectedResponse = new CashRegistersWorkSummaryDtoResponse("0d 5h 0m 0s");

        Mockito.when(cashRegisterService.getWorkSummary(from, to))
                .thenReturn(expectedResponse);

        var response = objectMapper.readValue(
                cashRegisterTestApi.getCashRegistersWorkSummary(
                                testDirector,
                                from.toString(),
                                to.toString()
                        )
                        .andExpect(status().is2xxSuccessful())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                CashRegistersWorkSummaryDtoResponse.class
        );

        Assertions.assertEquals(expectedResponse, response);
    }

    private static Stream<Arguments> getInvalidGetWorkSummaryRequests() {
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        List.of(
                                new ErrorDtoResponse(
                                        ErrorCode.QUERY_PARAMETER_REQUIRED,
                                        ErrorCode.QUERY_PARAMETER_REQUIRED.getMessage(),
                                        "from"
                                )
                        )
                ),
                Arguments.of(
                        Instant.now().toString(),
                        null,
                        List.of(
                                new ErrorDtoResponse(
                                        ErrorCode.QUERY_PARAMETER_REQUIRED,
                                        ErrorCode.QUERY_PARAMETER_REQUIRED.getMessage(),
                                        "to"
                                )
                        )
                ),
                Arguments.of(
                        "invalidDate",
                        "invalidDate",
                        List.of(
                                new ErrorDtoResponse(
                                        ErrorCode.QUERY_PARAMETER_INVALID,
                                        ErrorCode.QUERY_PARAMETER_INVALID.getMessage(),
                                        "from"
                                )
                        )
                ),
                Arguments.of(
                        Instant.now().toString(),
                        "invalidDate",
                        List.of(
                                new ErrorDtoResponse(
                                        ErrorCode.QUERY_PARAMETER_INVALID,
                                        ErrorCode.QUERY_PARAMETER_INVALID.getMessage(),
                                        "to"
                                )
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidGetWorkSummaryRequests")
    void whenInvalid_getWorkSummary_shouldReturnError(
            String from,
            String to,
            List<ErrorDtoResponse> expectedErrors
    ) throws Exception {

        var response = objectMapper.readValue(
                cashRegisterTestApi.getCashRegistersWorkSummary(testDirector, from, to)
                        .andExpect(status().isBadRequest())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                ErrorsDtoResponse.class
        );

        Mockito.verify(cashRegisterService, Mockito.never()).getWorkSummary(
                Mockito.any(),
                Mockito.any()
        );

        Assertions.assertEquals(expectedErrors.size(), response.getErrors().size());
        Assertions.assertTrue(response.getErrors().containsAll(expectedErrors));
    }
}
