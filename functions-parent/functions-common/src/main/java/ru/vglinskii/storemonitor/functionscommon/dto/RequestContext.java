package ru.vglinskii.storemonitor.functionscommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestContext {
    private AuthorizationContextDto authorizer;
}
