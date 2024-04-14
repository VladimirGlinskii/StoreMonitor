package ru.vglinskii.storemonitor.baseapi.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import ru.vglinskii.storemonitor.baseapi.auth.AuthorizationContextHolder;

@TestConfiguration
@Import({AuthorizationContextHolder.class})
public class ControllerTestConfiguration {
}
