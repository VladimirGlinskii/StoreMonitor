package ru.vglinskii.storemonitor.createincident.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.common.enums.IncidentType;
import ru.vglinskii.storemonitor.createincident.utils.ValidationErrorMessages;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateIncidentDtoRequest {
    @NotNull(message = ValidationErrorMessages.REQUIRED_FIELD)
    private IncidentType eventType;
    @NotNull(message = ValidationErrorMessages.REQUIRED_FIELD)
    private String description;
    @NotNull(message = ValidationErrorMessages.REQUIRED_FIELD)
    private Instant datetime;
    private boolean calledPolice;
    private boolean calledAmbulance;
    private boolean calledFireDepartment;
    private boolean calledGasService;
}
