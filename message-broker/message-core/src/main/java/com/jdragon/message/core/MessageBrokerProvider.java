package com.jdragon.message.core;

import java.util.Map;

public interface MessageBrokerProvider {

    String getType();

    MessageBroker createMessageBroker(Map<String,Object> config);

}
