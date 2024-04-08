package ru.vglinskii.storemonitor.incidentsreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncidentsReportDtoResponse {
    private int totalIncidents;
    private int calledPolice;
    private int calledAmbulance;
    private int calledFireDepartment;
    private int calledGasService;
}
