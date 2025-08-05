package com.jdragon.message.core;

public interface MessageBroker {
    void subscribe(String topic, MessageListener listener);
    void unsubscribe(String topic);
    void publish(String topic, String message);
}
