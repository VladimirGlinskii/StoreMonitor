package ru.vglinskii.storemonitor.baseapi.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.vglinskii.storemonitor.baseapi.utils.ApplicationConstants;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);
    private final ObjectMapper objectMapper;
    private final AuthorizationContextHolder contextHolder;

    public AuthInterceptor(
            ObjectMapper objectMapper,
            AuthorizationContextHolder contextHolder
    ) {
        this.objectMapper = objectMapper;
        this.contextHolder = contextHolder;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String authContextJson = request.getHeader(ApplicationConstants.AUTH_CONTEXT_HEADER);
        AuthorizationContextDto authContext = parseAuthContext(authContextJson);
        boolean isValidRequest = authContext != null;

        if (isValidRequest) {
            LOGGER.info("Set auth context for employee {}", authContext.getEmployeeId());
            contextHolder.setContext(authContext);
            MDC.put("employeeId", String.valueOf(authContext.getEmployeeId()));
            MDC.put("storeId", String.valueOf(authContext.getStoreId()));
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

        return isValidRequest;
    }

    private AuthorizationContextDto parseAuthContext(String authContextBase64) {
        try {
            String authContextJson = new String(Base64.decodeBase64(authContextBase64));
            return objectMapper.readValue(
                    authContextJson,
                    AuthorizationContextDto.class
            );
        } catch (JsonProcessingException | NullPointerException e) {
            LOGGER.error("Failed to parse authorization context", e);
            return null;
        }
    }
}
