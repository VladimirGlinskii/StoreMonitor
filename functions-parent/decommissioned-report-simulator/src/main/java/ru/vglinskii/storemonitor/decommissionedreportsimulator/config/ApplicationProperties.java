package ru.vglinskii.storemonitor.decommissionedreportsimulator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;

@Getter
@AllArgsConstructor
public class ApplicationProperties extends CommonApplicationProperties {
    private final String bucketName;
    private final String saAccessKey;
    private final String saSecretKey;
    private final int maxCommoditiesForDecommissionCount;

    public ApplicationProperties() {
        super();

        this.bucketName = getEnvValue("BUCKET_NAME");
        this.saAccessKey = getEnvValue("SA_ACCESS_KEY");
        this.saSecretKey = getEnvValue("SA_SECRET_KEY");
        this.maxCommoditiesForDecommissionCount = getEnvValue(
                "MAX_COMMODITIES_FOR_DECOMMISSION_COUNT",
                Integer::parseInt,
                10
        );
    }
}
