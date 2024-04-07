package ru.vglinskii.storemonitor.functionscommon.utils.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AppObjectMapper extends ObjectMapper {
    public AppObjectMapper() {
        super();

        findAndRegisterModules();
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
