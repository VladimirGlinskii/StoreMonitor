package ru.vglinskii.storemonitor.baseapi.controller;

import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValueDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.sensor.SensorWithValuesDtoResponse;
import ru.vglinskii.storemonitor.baseapi.service.SensorService;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    public List<SensorWithValueDtoResponse> getSensors() {
        return sensorService.getSensorsWithCurrentValue();
    }

    @GetMapping("temperature")
    public List<SensorWithValuesDtoResponse> getTemperatureReport(
            @RequestParam() Instant from,
            @RequestParam() Instant to
    ) {
        return sensorService.getTemperatureReport(from, to);
    }
}
