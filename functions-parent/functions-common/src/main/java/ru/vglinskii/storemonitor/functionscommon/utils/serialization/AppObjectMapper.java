package ru.vglinskii.storemonitor.functionscommon.utils.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppObjectMapper extends ObjectMapper {
    public AppObjectMapper() {
        super();

        SimpleModule validationModule = new SimpleModule();
        validationModule.setDeserializerModifier(new ValidationBeanDeserializerModifier());
        registerModule(validationModule);

        JavaTimeModule javaTimeModule= new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        registerModule(javaTimeModule);
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
