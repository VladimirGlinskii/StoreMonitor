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
import ru.vglinskii.storemonitor.baseapi.TestBase;
import ru.vglinskii.storemonitor.baseapi.dto.ErrorDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.ErrorsDtoResponse;
import ru.vglinskii.storemonitor.baseapi.exception.ErrorCode;
import ru.vglinskii.storemonitor.baseapi.service.SensorService;
import ru.vglinskii.storemonitor.baseapi.utils.SensorTestApi;

@WebMvcTest(SensorController.class)
@Import(SensorTestApi.class)
public class SensorControllerTest extends TestBase {
    private final static long TEST_STORE_ID = 1;
    @MockBean
    private SensorService sensorService;
    @Autowired
    private SensorTestApi sensorTestApi;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenValid_getTemperatureReport_shouldSuccess() throws Exception {
        var from = Instant.now().minus(5, ChronoUnit.HOURS);
        var to = Instant.now();

        Mockito.when(sensorService.getTemperatureReport(TEST_STORE_ID, from, to))
                .thenReturn(List.of());

        sensorTestApi.getTemperatureReport(TEST_STORE_ID, from.toString(), to.toString())
                .andExpect(status().is2xxSuccessful());
    }

    private static Stream<Arguments> getInvalidGetTemperatureReportRequests() {
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
    @MethodSource("getInvalidGetTemperatureReportRequests")
    void whenInvalid_getTemperatureReport_shouldReturnError(
            String from,
            String to,
            List<ErrorDtoResponse> expectedErrors
    ) throws Exception {

        var response = objectMapper.readValue(
                sensorTestApi.getTemperatureReport(TEST_STORE_ID, from, to)
                        .andExpect(status().isBadRequest())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                ErrorsDtoResponse.class
        );

        Mockito.verify(sensorService, Mockito.never()).getTemperatureReport(
                Mockito.anyLong(),
                Mockito.any(),
                Mockito.any()
        );

        Assertions.assertEquals(expectedErrors.size(), response.getErrors().size());
        Assertions.assertTrue(response.getErrors().containsAll(expectedErrors));
    }
}
