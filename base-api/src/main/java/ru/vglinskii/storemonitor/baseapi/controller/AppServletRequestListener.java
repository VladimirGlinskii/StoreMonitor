package ru.vglinskii.storemonitor.baseapi.controller;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class AppServletRequestListener implements ServletRequestListener {
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        MDC.clear();
    }
}
