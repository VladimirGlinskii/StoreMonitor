package ru.vglinskii.storemonitor.decommissionedreportsimulator.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Commodity {
    private UUID id;
    private String name;
}
