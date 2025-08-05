package com.jdragon.message.core;

@FunctionalInterface
public interface MessageListener {
    void onMessage(String topic, String message);
}
