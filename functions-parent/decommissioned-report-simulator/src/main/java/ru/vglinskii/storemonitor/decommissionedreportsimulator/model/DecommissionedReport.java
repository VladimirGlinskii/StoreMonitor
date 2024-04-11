package ru.vglinskii.storemonitor.decommissionedreportsimulator.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DecommissionedReport {
    private Long id;
    private Instant createdAt;
    private String link;
    private long storeId;
}
