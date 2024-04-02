package ru.vglinskii.storemonitor.functionscommon.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.vglinskii.storemonitor.functionscommon.utils.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNKNOWN_SERVER_ERROR("Something went wrong on server", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("Invalid request"),
    FIELD_NOT_VALID("This field value is not valid");

    private final String message;
    private final int httpStatus;

    ErrorCode(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }
}
