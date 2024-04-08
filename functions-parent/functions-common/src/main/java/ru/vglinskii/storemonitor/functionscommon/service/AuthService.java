package ru.vglinskii.storemonitor.functionscommon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;
import ru.vglinskii.storemonitor.functionscommon.dto.HttpRequestDto;
import ru.vglinskii.storemonitor.functionscommon.exception.AppRuntimeException;
import ru.vglinskii.storemonitor.functionscommon.exception.ErrorCode;

public class AuthService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private ObjectMapper objectMapper;

    public AuthService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthorizationContextDto parseAuthContext(HttpRequestDto request) {
        try {
            return objectMapper.readValue(
                    (String) request.getRequestContext().get("authorizer"),
                    AuthorizationContextDto.class
            );
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse authorization context", e);
            throw new AppRuntimeException(ErrorCode.AUTHORIZATION_CONTEXT_REQUIRED);
        }
    }
}
