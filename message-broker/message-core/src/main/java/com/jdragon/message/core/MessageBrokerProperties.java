package com.jdragon.message.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "message.broker")
@Component
public class MessageBrokerProperties {

    private String type;

    private Map<String, Object> config = new HashMap<>();

}
