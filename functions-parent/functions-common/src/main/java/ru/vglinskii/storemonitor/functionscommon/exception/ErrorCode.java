package ru.vglinskii.storemonitor.functionscommon.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNKNOWN_SERVER_ERROR("Something went wrong on server", HttpStatus.SC_INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("Invalid request"),
    AUTHORIZATION_CONTEXT_REQUIRED("Authorization context is required"),
    FIELD_NOT_VALID("This field value is not valid");

    private final String message;
    private final int httpStatus;

    ErrorCode(String message) {
        this(message, HttpStatus.SC_BAD_REQUEST);
    }
}
