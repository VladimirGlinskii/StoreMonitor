package ru.vglinskii.storemonitor.functionscommon.utils.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class AppObjectMapper extends ObjectMapper {
    public AppObjectMapper() {
        super();

        SimpleModule validationModule = new SimpleModule();
        validationModule.setDeserializerModifier(new ValidationBeanDeserializerModifier());
        registerModule(validationModule);

        findAndRegisterModules();
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
