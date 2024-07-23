package org.example.worktest.utils.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.worktest.utils.ReflectionUtils;

import java.io.IOException;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExtStringSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private ExtStringHandler extStringHandler;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        CustomerJacksonSerializerEnum type = extStringHandler.type();
        String className = type.getClassName();
        Object reflectObject = ReflectionUtils.objectForName(className);
        CustomerJacksonSerializer customerJacksonSerializer = (CustomerJacksonSerializer) reflectObject;
        if (customerJacksonSerializer != null && extStringHandler.serializer()) {
            value = customerJacksonSerializer.serialize(value, extStringHandler);
        }
        gen.writeString(value);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty beanProperty) throws JsonMappingException {
        if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
            extStringHandler = beanProperty.getAnnotation(ExtStringHandler.class);
            if (null == extStringHandler) {
                extStringHandler = beanProperty.getContextAnnotation(ExtStringHandler.class);
            }
            if (extStringHandler != null) {
                return this;
            }
        }

        return serializers.findValueSerializer(beanProperty.getType(), beanProperty);
    }
}
