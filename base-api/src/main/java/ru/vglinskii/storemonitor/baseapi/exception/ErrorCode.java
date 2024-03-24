package ru.vglinskii.storemonitor.baseapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNKNOWN_SERVER_ERROR("Something went wrong on server", HttpStatus.INTERNAL_SERVER_ERROR),
    FIELD_NOT_VALID("This field value is not valid"),
    QUERY_PARAMETER_REQUIRED("This query parameter is required"),
    QUERY_PARAMETER_INVALID("This query parameter is invalid"),
    CASH_REGISTER_NOT_FOUND("Cash register not found", HttpStatus.NOT_FOUND),
    CASH_REGISTER_EXISTS("Cash register with this inventory number already exists"),
    CASHIER_NOT_FOUND("Cashier not found", HttpStatus.NOT_FOUND),
    CASH_REGISTER_OPENED_BY_OTHER("Cash register is opened by other cashier", HttpStatus.FORBIDDEN),
    CASH_REGISTER_ALREADY_CLOSED("Cash register is already closed");

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }
}
