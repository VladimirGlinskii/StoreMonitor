package ru.vglinskii.storemonitor.functionscommon.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

@SuperBuilder
@Getter
@Setter
public class Employee extends BaseEntity {
    private String secret;
    private String firstName;
    private String lastName;
    private long storeId;
    private EmployeeType type;
}
