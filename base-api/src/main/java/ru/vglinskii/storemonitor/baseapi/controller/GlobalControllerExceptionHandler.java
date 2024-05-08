package ru.vglinskii.storemonitor.baseapi.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.vglinskii.storemonitor.baseapi.dto.ErrorDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.ErrorsDtoResponse;
import ru.vglinskii.storemonitor.baseapi.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.baseapi.exception.ErrorCode;

@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(value = {AppRuntimeException.class})
    public ResponseEntity<ErrorsDtoResponse> handleAppRuntimeException(AppRuntimeException e) {
        log.info("App runtime exception:", e);
        return new ResponseEntity<>(
                new ErrorsDtoResponse(new ErrorDtoResponse(e.getErrorCode())),
                e.getErrorCode().getHttpStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorsDtoResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        log.info("Validation exception:", ex);
        List<ErrorDtoResponse> errors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            errors.add(
                    new ErrorDtoResponse(
                            ErrorCode.FIELD_NOT_VALID,
                            error.getDefaultMessage(),
                            ((FieldError) error).getField()
                    )
            );
        });

        return new ResponseEntity<>(new ErrorsDtoResponse(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorsDtoResponse> handleValidationExceptions(
            MissingServletRequestParameterException ex
    ) {
        log.info("Validation exception:", ex);
        return new ResponseEntity<>(
                new ErrorsDtoResponse(new ErrorDtoResponse(
                        ErrorCode.QUERY_PARAMETER_REQUIRED,
                        ErrorCode.QUERY_PARAMETER_REQUIRED.getMessage(),
                        ex.getParameterName()
                )),
                ErrorCode.QUERY_PARAMETER_REQUIRED.getHttpStatus()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorsDtoResponse> handleValidationExceptions(
            MethodArgumentTypeMismatchException ex
    ) {
        log.info("Validation exception:", ex);
        return new ResponseEntity<>(
                new ErrorsDtoResponse(new ErrorDtoResponse(
                        ErrorCode.QUERY_PARAMETER_INVALID,
                        ErrorCode.QUERY_PARAMETER_INVALID.getMessage(),
                        ex.getName()
                )),
                ErrorCode.QUERY_PARAMETER_INVALID.getHttpStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorsDtoResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return new ResponseEntity<>(
                new ErrorsDtoResponse(new ErrorDtoResponse(ErrorCode.UNKNOWN_SERVER_ERROR)),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
