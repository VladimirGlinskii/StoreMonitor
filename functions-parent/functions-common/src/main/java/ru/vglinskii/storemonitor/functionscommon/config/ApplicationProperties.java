package ru.vglinskii.storemonitor.functionscommon.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationProperties {
    private final String dbUrl;
    private final String dbPassword;
    private final String dbUser;

    public ApplicationProperties() {
        this(
                System.getenv("DB_URL"),
                System.getenv("DB_PASSWORD"),
                System.getenv("DB_USERNAME")
        );
    }
}
