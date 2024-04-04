package ru.vglinskii.storemonitor.authfunction.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;
import ru.vglinskii.storemonitor.functionscommon.model.BaseEntity;

@SuperBuilder
@Getter
@Setter
public class Employee extends BaseEntity {
    private String secret;
    private long storeId;
    private EmployeeType type;
}
