package ru.vglinskii.storemonitor.functionscommon.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppRuntimeException extends RuntimeException {
    private final ErrorCode errorCode;
}
