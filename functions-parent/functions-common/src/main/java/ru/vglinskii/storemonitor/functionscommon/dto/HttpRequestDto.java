package ru.vglinskii.storemonitor.functionscommon.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestDto {
    private String body;
    private Map<String, String> headers;
    private Map<String, String> params;
    private RequestContext requestContext;
    private Map<String, String> queryStringParameters;
}
