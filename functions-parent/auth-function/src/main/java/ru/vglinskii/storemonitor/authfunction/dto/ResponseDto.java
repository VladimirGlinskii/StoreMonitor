package ru.vglinskii.storemonitor.authfunction.dto;

import lombok.Builder;
import lombok.Data;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;

@Data
@Builder
public class ResponseDto {
    private boolean isAuthorized;
    private AuthorizationContextDto context;
}
