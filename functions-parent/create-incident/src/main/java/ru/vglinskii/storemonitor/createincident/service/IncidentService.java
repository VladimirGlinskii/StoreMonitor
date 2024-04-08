package ru.vglinskii.storemonitor.createincident.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.createincident.dao.IncidentDao;
import ru.vglinskii.storemonitor.createincident.dto.CreateIncidentDtoRequest;
import ru.vglinskii.storemonitor.createincident.model.Incident;

public class IncidentService {
    private final static Logger LOGGER = LoggerFactory.getLogger(IncidentService.class);
    private IncidentDao incidentDao;

    public IncidentService(IncidentDao incidentDao) {
        this.incidentDao = incidentDao;
    }

    public void createIncident(CreateIncidentDtoRequest request, long storeId) {
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
        incidentDao.create(incident);
    }
}
