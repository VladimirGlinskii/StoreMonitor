package ru.vglinskii.storemonitor.cashiersimulator.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpResponse {
    private int status;
    private String body;

    public boolean is2xxSuccessful() {
        return status >= 200 && status < 300;
    }
}
