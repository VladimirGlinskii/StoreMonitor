package ru.vglinskii.storemonitor.createincident.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.vglinskii.storemonitor.common.enums.IncidentType;

@Builder
@Getter
@Setter
public class Incident {
    private Long id;
    private long storeId;
    private IncidentType eventType;
    private String description;
    private Instant datetime;
    private boolean calledPolice;
    private boolean calledAmbulance;
    private boolean calledFireDepartment;
    private boolean calledGasService;
}
