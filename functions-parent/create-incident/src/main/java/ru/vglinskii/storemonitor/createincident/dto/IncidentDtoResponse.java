package ru.vglinskii.storemonitor.createincident.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.common.enums.IncidentType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncidentDtoResponse {
    private long id;
    private long storeId;
    private IncidentType eventType;
    private String description;
    private Instant datetime;
    private boolean calledPolice;
    private boolean calledAmbulance;
    private boolean calledFireDepartment;
    private boolean calledGasService;
}
