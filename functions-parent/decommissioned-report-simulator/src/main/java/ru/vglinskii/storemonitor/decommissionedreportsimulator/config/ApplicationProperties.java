package ru.vglinskii.storemonitor.decommissionedreportsimulator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationProperties extends ru.vglinskii.storemonitor.functionscommon.config.ApplicationProperties {
    private final String bucketName;
    private final String saAccessKey;
    private final String saSecretKey;

    public ApplicationProperties() {
        super();

        this.bucketName = getEnvValue("BUCKET_NAME");
        this.saAccessKey = getEnvValue("SA_ACCESS_KEY");
        this.saSecretKey = getEnvValue("SA_SECRET_KEY");
    }
}
