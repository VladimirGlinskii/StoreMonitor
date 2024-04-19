package ru.vglinskii.storemonitor.functionscommon.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.dto.ErrorDtoResponse;
import ru.vglinskii.storemonitor.functionscommon.dto.ErrorsDtoResponse;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpResponseDto;

public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HttpResponseDto handle(Supplier<HttpResponseDto> function) {
        try {
            return function.get();
        } catch (AppRuntimeException e) {
            return handleAppRuntimeException(e);
        } catch (ConstraintViolationException e) {
            return handleValidationExceptions(e);
        } catch (Throwable e) {
            return handleUnknownException(e);
        }
    }

    public HttpResponseDto handleAppRuntimeException(AppRuntimeException e) {
        try {
            return new HttpResponseDto(
                    e.getErrorCode().getHttpStatus(),
                    objectMapper.writeValueAsString(
                            new ErrorsDtoResponse(new ErrorDtoResponse(e.getErrorCode()))
                    ),
                    Map.ofEntries(Map.entry(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
            );
        } catch (JsonProcessingException ex) {
            return handleUnknownException(ex);
        }
    }

    public HttpResponseDto handleValidationExceptions(ConstraintViolationException e) {
        List<ErrorDtoResponse> errors = new ArrayList<>();

        e.getConstraintViolations().forEach((error) -> {
            errors.add(
                    new ErrorDtoResponse(
                            ErrorCode.FIELD_NOT_VALID,
                            error.getMessage(),
                            error.getPropertyPath().toString()
                    )
            );
        });

        try {
            return new HttpResponseDto(
                    HttpStatus.SC_BAD_REQUEST,
                    objectMapper.writeValueAsString(
                            new ErrorsDtoResponse(errors)
                    ),
                    Map.ofEntries(Map.entry(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
            );
        } catch (JsonProcessingException ex) {
            return handleUnknownException(ex);
        }
    }

    public HttpResponseDto handleUnknownException(Throwable e) {
        LOGGER.error("Internal server error", e);


        try {
            return new HttpResponseDto(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    objectMapper.writeValueAsString(
                            new ErrorsDtoResponse(new ErrorDtoResponse(ErrorCode.UNKNOWN_SERVER_ERROR))
                    ),
                    Map.ofEntries(Map.entry(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
            );
        } catch (JsonProcessingException ex) {
            return handleUnknownException(ex);
        }
    }
}
