package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vglinskii.storemonitor.baseapi.auth.AuthorizationContextHolder;
import ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport.DecommissionedReportDtoResponse;
import ru.vglinskii.storemonitor.baseapi.repository.DecommissionedReportRepository;

@Service
@Slf4j
public class DecommissionedReportService {
    private final DecommissionedReportRepository decommissionedReportRepository;
    private final AuthorizationContextHolder authorizationContextHolder;

    public DecommissionedReportService(
            DecommissionedReportRepository decommissionedReportRepository,
            AuthorizationContextHolder authorizationContextHolder
    ) {
        this.decommissionedReportRepository = decommissionedReportRepository;
        this.authorizationContextHolder = authorizationContextHolder;
    }

    public List<DecommissionedReportDtoResponse> getAll(Instant from, Instant to) {
        var storeId = authorizationContextHolder.getContext().getStoreId();
        log.info("Received get all decommissioned reports request for store {}", storeId);
        return decommissionedReportRepository.findByStoreIdInInterval(storeId, from, to).stream()
                .map((r) -> DecommissionedReportDtoResponse.builder()
                        .link(r.getLink())
                        .datetime(r.getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
