package ru.vglinskii.storemonitor.createincident.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.createincident.dto.CreateIncidentDtoRequest;
import ru.vglinskii.storemonitor.createincident.dto.IncidentDtoResponse;
import ru.vglinskii.storemonitor.functionscommon.dao.CommonIncidentDao;
import ru.vglinskii.storemonitor.functionscommon.model.Incident;

public class IncidentService {
    private final static Logger LOGGER = LoggerFactory.getLogger(IncidentService.class);
    private CommonIncidentDao incidentDao;

    public IncidentService(CommonIncidentDao incidentDao) {
        this.incidentDao = incidentDao;
    }

    public IncidentDtoResponse createIncident(CreateIncidentDtoRequest request, long storeId) {
        LOGGER.info("Creating incident {} for store {}", request.getEventType(), storeId);
        var incident = Incident.builder()
                .storeId(storeId)
                .datetime(request.getDatetime())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .calledAmbulance(request.isCalledAmbulance())
                .calledPolice(request.isCalledPolice())
                .calledFireDepartment(request.isCalledFireDepartment())
                .calledGasService(request.isCalledGasService())
                .build();
        incident = incidentDao.create(incident);

        return IncidentDtoResponse.builder()
                .id(incident.getId())
                .storeId(incident.getStoreId())
                .datetime(incident.getDatetime())
                .description(incident.getDescription())
                .eventType(incident.getEventType())
                .calledAmbulance(incident.isCalledAmbulance())
                .calledFireDepartment(incident.isCalledFireDepartment())
                .calledGasService(incident.isCalledGasService())
                .calledPolice(incident.isCalledPolice())
                .build();
    }
}
