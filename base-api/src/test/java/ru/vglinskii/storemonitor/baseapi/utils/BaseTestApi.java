package ru.vglinskii.storemonitor.baseapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;

public abstract class BaseTestApi {
    @Autowired
    protected ObjectMapper objectMapper;

    protected <T> String toJson(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "";
        }
    }

    protected MockHttpServletRequestBuilder authorized(
            MockHttpServletRequestBuilder rb,
            Employee employee
    ) {
        if (employee == null) {
            return rb;
        }

        var authContext = new AuthorizationContextDto(
                employee.getStore().getId(),
                employee.getId()
        );
        try {
            rb.header(
                    ApplicationConstants.AUTH_CONTEXT_HEADER,
                    Base64.encodeBase64String(
                            objectMapper.writeValueAsString(authContext).getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (JsonProcessingException ignored) {
        }

        return rb;
    }
}
