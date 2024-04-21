package ru.vglinskii.storemonitor.cashiersimulator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;

@Getter
@AllArgsConstructor
public class ApplicationProperties extends CommonApplicationProperties {
    private final String baseApiUrl;

    public ApplicationProperties() {
        super();
        this.baseApiUrl = getEnvValue("BASE_API_URL");
    }
}
