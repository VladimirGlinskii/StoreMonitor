package ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecommissionedReportsDtoResponse {
    private List<DecommissionedReportDtoResponse> reports;
}
