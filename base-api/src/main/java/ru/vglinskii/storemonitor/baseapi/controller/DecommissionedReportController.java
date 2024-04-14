package ru.vglinskii.storemonitor.baseapi.controller;

import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport.DecommissionedReportDtoResponse;
import ru.vglinskii.storemonitor.baseapi.service.DecommissionedReportService;

@RestController
@RequestMapping("/api/decommissioned-reports")
public class DecommissionedReportController {
    private final DecommissionedReportService decommissionedReportService;

    public DecommissionedReportController(DecommissionedReportService decommissionedReportService) {
        this.decommissionedReportService = decommissionedReportService;
    }

    @GetMapping
    public List<DecommissionedReportDtoResponse> getAll(
            @RequestParam() Instant from,
            @RequestParam() Instant to
    ) {
        return decommissionedReportService.getAll(from, to);
    }
}
