package org.example.worktest.utils.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@JacksonAnnotationsInside
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JsonSerialize(using = ExtStringSerializer.class)
@JsonDeserialize(using = ExtStringDeserializer.class)
public @interface ExtStringHandler {

    CustomerJacksonSerializerEnum type();

    boolean deserializer() default true;

    boolean serializer() default true;
}
