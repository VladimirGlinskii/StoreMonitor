package ru.vglinskii.storemonitor.sensorsimulator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;

@Getter
@AllArgsConstructor
public class ApplicationProperties extends CommonApplicationProperties {
    private final String devicesApiUrl;
    private final float sensorValueCelsiusMean;
    private final float sensorValueCelsiusStandardDeviation;

    public ApplicationProperties() {
        super();

        this.devicesApiUrl = getEnvValue("DEVICES_API_URL");
        this.sensorValueCelsiusMean = getEnvValue(
                "SENSOR_VALUE_CELSIUS_MEAN",
                Float::parseFloat,
                0f
        );
        this.sensorValueCelsiusStandardDeviation = getEnvValue(
                "SENSOR_VALUE_CELSIUS_STANDARD_DEVIATION",
                Float::parseFloat,
                6f
        );
    }
}
