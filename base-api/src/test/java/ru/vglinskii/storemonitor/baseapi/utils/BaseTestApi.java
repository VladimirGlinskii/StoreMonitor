package ru.vglinskii.storemonitor.baseapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseTestApi {
    @Autowired
    protected ObjectMapper objectMapper;

    protected <T> String toJson(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "";
        }
    }
}
