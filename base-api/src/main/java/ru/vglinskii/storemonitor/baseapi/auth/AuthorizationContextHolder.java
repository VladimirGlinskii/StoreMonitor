package ru.vglinskii.storemonitor.baseapi.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import ru.vglinskii.storemonitor.common.dto.AuthorizationContextDto;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@RequestScope
@Component
public class AuthorizationContextHolder {
    private AuthorizationContextDto context;
}
