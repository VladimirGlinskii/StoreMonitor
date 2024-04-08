package ru.vglinskii.storemonitor.incidentsreport.service;

import java.time.Instant;
import ru.vglinskii.storemonitor.incidentsreport.dao.IncidentDao;
import ru.vglinskii.storemonitor.incidentsreport.dto.IncidentsReportDtoResponse;

public class IncidentService {
    private IncidentDao incidentDao;

    public IncidentService(IncidentDao incidentDao) {
        this.incidentDao = incidentDao;
    }

    public IncidentsReportDtoResponse getIncidentsReport(long storeId, Instant from, Instant to) {
        var incidents = incidentDao.findByStoreIdInInterval(storeId, from, to);
        var responseDto = new IncidentsReportDtoResponse();
        responseDto.setTotalIncidents(incidents.size());

        for (var incident : incidents) {
            if (incident.isCalledAmbulance()) {
                responseDto.setCalledAmbulance(responseDto.getCalledAmbulance() + 1);
            }
            if (incident.isCalledPolice()) {
                responseDto.setCalledPolice(responseDto.getCalledPolice() + 1);
            }
            if (incident.isCalledGasService()) {
                responseDto.setCalledGasService(responseDto.getCalledGasService() + 1);
            }
            if (incident.isCalledFireDepartment()) {
                responseDto.setCalledFireDepartment(responseDto.getCalledFireDepartment() + 1);
            }
        }

        return responseDto;
    }
}
