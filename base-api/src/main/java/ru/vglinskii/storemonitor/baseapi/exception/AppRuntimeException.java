package ru.vglinskii.storemonitor.baseapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppRuntimeException extends RuntimeException {
    private final ErrorCode errorCode;
}
