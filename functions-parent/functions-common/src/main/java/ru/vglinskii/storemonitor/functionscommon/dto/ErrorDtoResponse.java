package ru.vglinskii.storemonitor.functionscommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.functionscommon.exception.ErrorCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDtoResponse {
    private ErrorCode errorCode;
    private String message;
    private String field;

    public ErrorDtoResponse(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
        this.field = "";
    }
}
