package ru.vglinskii.storemonitor.baseapi.controller;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorsWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorsWithValuesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.service.SensorService;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    public SensorsWithValueDtoResponse getSensors() {
        return sensorService.getSensorsWithCurrentValue();
    }

    @GetMapping("temperature")
    public SensorsWithValuesDtoResponse getTemperatureReport(
            @RequestParam() Instant from,
            @RequestParam() Instant to
    ) {
        return sensorService.getTemperatureReport(from, to);
    }
}
