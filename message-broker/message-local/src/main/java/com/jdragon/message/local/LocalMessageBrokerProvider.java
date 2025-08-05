package com.jdragon.message.local;

import com.jdragon.message.core.MessageBroker;
import com.jdragon.message.core.MessageBrokerProvider;
import lombok.Getter;

import java.util.Map;

@Getter
public class LocalMessageBrokerProvider implements MessageBrokerProvider {

    private final String type = "local";

    @Override
    public MessageBroker createMessageBroker(Map<String, Object> config) {
        LocalConfig localConfig = new LocalConfig();
        localConfig.setPoolNamePrefix((String) config.get("poolNamePrefix"));
        localConfig.setThreadPoolSize((Integer) config.get("threadPoolSize"));
        return new LocalMessageBroker(localConfig);
    }
}
