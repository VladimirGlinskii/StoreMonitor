package ru.vglinskii.storemonitor.functionscommon.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto {
    private int statusCode;
    private String body;
    private Map<String, String> headers;

    public ResponseDto(int statusCode, Map<String, String> headers) {
        this(statusCode, "", headers);
    }
}
