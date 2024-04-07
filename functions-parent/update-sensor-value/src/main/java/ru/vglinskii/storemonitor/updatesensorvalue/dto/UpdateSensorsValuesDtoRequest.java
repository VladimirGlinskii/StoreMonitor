package ru.vglinskii.storemonitor.updatesensorvalue.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.updatesensorvalue.utils.ValidationErrorMessages;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSensorsValuesDtoRequest {
    @Valid
    @NotNull(message = ValidationErrorMessages.REQUIRED_FIELD)
    private List<UpdateSensorValueDtoRequest> values;
}
