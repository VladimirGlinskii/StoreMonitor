package ru.vglinskii.storemonitor.functionscommon.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpResponse {
    private int status;
    private String body;

    public boolean is2xxSuccessful() {
        return status >= 200 && status < 300;
    }
}
