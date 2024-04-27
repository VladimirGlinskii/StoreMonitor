package ru.vglinskii.storemonitor.sensorsimulator.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorSimulatorConfig {
    private final float sensorValueCelsiusMean;
    private final float sensorValueCelsiusStandardDeviation;
}
