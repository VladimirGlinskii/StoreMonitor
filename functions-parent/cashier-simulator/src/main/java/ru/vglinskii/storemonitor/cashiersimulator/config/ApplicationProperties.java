package ru.vglinskii.storemonitor.cashiersimulator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationProperties extends ru.vglinskii.storemonitor.functionscommon.config.ApplicationProperties {
    private final String baseApiUrl;

    public ApplicationProperties() {
        super();
        this.baseApiUrl = getEnvValue("BASE_API_URL");
    }
}
