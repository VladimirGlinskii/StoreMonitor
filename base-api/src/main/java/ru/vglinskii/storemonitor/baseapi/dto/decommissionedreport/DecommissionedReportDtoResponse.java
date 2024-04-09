package ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecommissionedReportDtoResponse {
    private String link;
    private Instant datetime;
}
