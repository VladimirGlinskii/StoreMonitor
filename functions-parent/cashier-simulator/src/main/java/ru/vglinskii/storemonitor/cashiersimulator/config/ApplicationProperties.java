package ru.vglinskii.storemonitor.cashiersimulator.config;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;

@Getter
@SuperBuilder
public class ApplicationProperties extends CommonApplicationProperties {
    private final String baseApiUrl;

    public ApplicationProperties() {
        super();
        this.baseApiUrl = getEnvValue("BASE_API_URL");
    }
}
