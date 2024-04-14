package ru.vglinskii.storemonitor.baseapi.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import ru.vglinskii.storemonitor.baseapi.TestBase;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.baseapi.utils.ApplicationConstants;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;

public class IntegrationTestBase extends TestBase {
    @Autowired
    private ObjectMapper objectMapper;

    protected HttpHeaders createAuthorizationHeader(Employee employee) {
        var authContext = new AuthorizationContextDto(
                employee.getStore().getId(),
                employee.getId()
        );
        var headers = new HttpHeaders();
        try {
            headers.set(
                    ApplicationConstants.AUTH_CONTEXT_HEADER,
                    Base64.encodeBase64String(
                            objectMapper.writeValueAsString(authContext).getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (JsonProcessingException ignored) {
        }

        return headers;
    }
}
