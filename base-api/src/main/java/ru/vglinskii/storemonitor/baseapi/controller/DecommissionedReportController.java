package ru.vglinskii.storemonitor.baseapi.controller;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport.DecommissionedReportDtoResponse;
import ru.vglinskii.storemonitor.baseapi.service.DecommissionedReportService;

@RestController
@RequestMapping("/api/decommissioned-reports")
@Slf4j
public class DecommissionedReportController {
    private final DecommissionedReportService decommissionedReportService;

    public DecommissionedReportController(DecommissionedReportService decommissionedReportService) {
        this.decommissionedReportService = decommissionedReportService;
    }

    @GetMapping
    public List<DecommissionedReportDtoResponse> getAll(
            @RequestHeader("X-Store-Id") long storeId,
            @RequestParam() Instant from,
            @RequestParam() Instant to
    ) {
        log.info("Received get all decommissioned reports request for store {}", storeId);
        return decommissionedReportService.getAll(storeId, from, to);
    }
}
