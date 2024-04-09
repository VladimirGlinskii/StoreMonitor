package ru.vglinskii.storemonitor.baseapi.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport.DecommissionedReportDtoResponse;
import ru.vglinskii.storemonitor.baseapi.repository.DecommissionedReportRepository;

@Service
public class DecommissionedReportService {
    private final DecommissionedReportRepository decommissionedReportRepository;

    public DecommissionedReportService(
            DecommissionedReportRepository decommissionedReportRepository
    ) {
        this.decommissionedReportRepository = decommissionedReportRepository;
    }

    public List<DecommissionedReportDtoResponse> getAll(long storeId, Instant from, Instant to) {
        return decommissionedReportRepository.findByStoreIdInInterval(storeId, from, to).stream()
                .map((r) -> DecommissionedReportDtoResponse.builder()
                        .link(r.getLink())
                        .datetime(r.getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
