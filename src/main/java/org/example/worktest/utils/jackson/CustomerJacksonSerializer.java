package org.example.worktest.utils.jackson;

public interface CustomerJacksonSerializer {

    String serialize(String value, ExtStringHandler extStringHandler);

    String deserialize(String value, ExtStringHandler extStringHandler);

}
