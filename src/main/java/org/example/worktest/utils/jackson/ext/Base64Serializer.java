package org.example.worktest.utils.jackson.ext;

import lombok.extern.slf4j.Slf4j;
import org.example.worktest.utils.Base64Util;
import org.example.worktest.utils.jackson.CustomerJacksonSerializer;
import org.example.worktest.utils.jackson.ExtStringHandler;


@Slf4j
public class Base64Serializer implements CustomerJacksonSerializer {
    @Override
    public String serialize(String value, ExtStringHandler extStringHandler) {
        if (extStringHandler.serializer()) {
            value = Base64Util.encode(value);
        }
        return value;
    }

    @Override
    public String deserialize(String value, ExtStringHandler extStringHandler) {
        try {
            if (extStringHandler.deserializer()) {
                value = Base64Util.decode(value);
            }
        } catch (Exception e) {
            log.error("base64Encoded error", e);
        }

        return value;
    }
}
