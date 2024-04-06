package ru.vglinskii.storemonitor.functionscommon.model;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public abstract class BaseEntity {
    protected Long id;
    protected Instant createdAt;
    protected Instant updatedAt;
}
