package ru.vglinskii.storemonitor.functionscommon.config;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonApplicationProperties {
    private final String dbUrl;
    private final String dbPassword;
    private final String dbUser;

    public CommonApplicationProperties() {
        this.dbUrl = getEnvValue("DB_URL");
        this.dbPassword = getEnvValue("DB_PASSWORD");
        this.dbUser = getEnvValue("DB_USERNAME");
    }

    protected <T> T getEnvValue(String name, Function<String, T> transformer, T defaultValue) {
        try {
            var value = System.getenv(name);

            return (value != null) ? transformer.apply(value) : defaultValue;
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    protected String getEnvValue(String name) {
        return getEnvValue(name, (v) -> v, null);
    }
}
