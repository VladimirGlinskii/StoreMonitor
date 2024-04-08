package ru.vglinskii.storemonitor.incidentsreport.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Incident {
    private Long id;
    private boolean calledPolice;
    private boolean calledAmbulance;
    private boolean calledFireDepartment;
    private boolean calledGasService;
}
