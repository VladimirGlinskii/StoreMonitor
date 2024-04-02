package ru.vglinskii.storemonitor.functionscommon.utils.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import jakarta.validation.*;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.io.IOException;
import java.util.Set;

public class ValidationBeanDeserializer extends BeanDeserializer {
    private final Validator validator;

    protected ValidationBeanDeserializer(BeanDeserializerBase src) {
        super(src);

        try (ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext context) throws IOException {
        Object instance = super.deserialize(p, context);
        validate(instance);

        return instance;
    }

    private <T> void validate(T t) {
        Set<ConstraintViolation<T>> violations = validator.validate(t);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
