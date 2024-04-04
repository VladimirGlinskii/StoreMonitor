package ru.vglinskii.storemonitor.functionscommon.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpResponseDto {
    private int statusCode;
    private String body;
    private Map<String, String> headers;

    public HttpResponseDto(int statusCode, Map<String, String> headers) {
        this(statusCode, "", headers);
    }
}
