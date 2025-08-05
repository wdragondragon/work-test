package com.jdragon.message.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdragon.message.core.MessageBroker;
import com.jdragon.message.core.MessageBrokerProvider;
import lombok.Getter;

import java.util.Map;

@Getter
public class RedisMessageBrokerProvider implements MessageBrokerProvider {

    private final String type = "redis";

    @Override
    public MessageBroker createMessageBroker(Map<String, Object> config) {
        ObjectMapper objectMapper = new ObjectMapper();
        RedisConfig redisConfig = objectMapper.convertValue(config, RedisConfig.class);
        return new RedisMessageBroker(new RedisManager(redisConfig));
    }
}
