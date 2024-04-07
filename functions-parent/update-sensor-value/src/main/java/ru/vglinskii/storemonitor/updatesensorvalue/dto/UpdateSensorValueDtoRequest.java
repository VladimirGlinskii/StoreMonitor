package ru.vglinskii.storemonitor.updatesensorvalue.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.common.enums.SensorUnit;
import ru.vglinskii.storemonitor.updatesensorvalue.utils.ValidationErrorMessages;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSensorValueDtoRequest {
    @NotNull(message = ValidationErrorMessages.REQUIRED_FIELD)
    private Long sensorId;
    @NotNull(message = ValidationErrorMessages.REQUIRED_FIELD)
    private Float value;
    @NotNull(message = ValidationErrorMessages.REQUIRED_FIELD)
    private SensorUnit unit;
}
