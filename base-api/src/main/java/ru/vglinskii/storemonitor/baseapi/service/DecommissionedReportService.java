package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vglinskii.storemonitor.baseapi.auth.AuthorizationContextHolder;
import ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport.DecommissionedReportsDtoResponse;
import ru.vglinskii.storemonitor.baseapi.mapper.DecommissionedReportMapper;
import ru.vglinskii.storemonitor.baseapi.repository.DecommissionedReportRepository;

@Service
@Slf4j
public class DecommissionedReportService {
    private final DecommissionedReportRepository decommissionedReportRepository;
    private final AuthorizationContextHolder authorizationContextHolder;
    private final DecommissionedReportMapper decommissionedReportMapper;

    public DecommissionedReportService(
            DecommissionedReportRepository decommissionedReportRepository,
            AuthorizationContextHolder authorizationContextHolder,
            DecommissionedReportMapper decommissionedReportMapper) {
        this.decommissionedReportRepository = decommissionedReportRepository;
        this.authorizationContextHolder = authorizationContextHolder;
        this.decommissionedReportMapper = decommissionedReportMapper;
    }

    public DecommissionedReportsDtoResponse getAll(Instant from, Instant to) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        log.info("Received get all decommissioned reports request for store {}", storeId);
        return new DecommissionedReportsDtoResponse(
                decommissionedReportRepository.findByStoreIdInInterval(storeId, from, to).stream()
                        .map(decommissionedReportMapper::toReportDto)
                        .toList()
        );
    }
}
