package org.example.worktest.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.example.worktest.utils.ReflectionUtils;

import java.io.IOException;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class ExtStringDeserializer extends JsonDeserializer<String> implements ContextualDeserializer {

    private ExtStringHandler extStringHandler;

    @Override
    public String deserialize(JsonParser p, DeserializationContext context) throws IOException {
        CustomerJacksonSerializerEnum type = extStringHandler.type();
        String className = type.getClassName();
        Object reflectObject = ReflectionUtils.objectForName(className);
        CustomerJacksonSerializer customerJacksonSerializer = (CustomerJacksonSerializer) reflectObject;
        String value = p.getValueAsString();
        if (customerJacksonSerializer != null && extStringHandler.deserializer()) {
            value = customerJacksonSerializer.deserialize(value, extStringHandler);
        }
        return value;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
            extStringHandler = beanProperty.getAnnotation(ExtStringHandler.class);
            if (null == extStringHandler) {
                extStringHandler = beanProperty.getContextAnnotation(ExtStringHandler.class);
            }
            if (null != extStringHandler) {
                return this;
            }
        }
        return deserializationContext.findContextualValueDeserializer(beanProperty.getType(), beanProperty);
    }
}