package com.jdragon.message.local;


import com.jdragon.message.core.MessageBroker;
import com.jdragon.message.core.MessageListener;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class LocalMessageBroker implements MessageBroker {

    private final Map<String, MessageListener> listeners = new ConcurrentHashMap<>();

    private final ExecutorService executor;

    public LocalMessageBroker(LocalConfig localConfig) {
        ThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern(localConfig.getPoolNamePrefix() + "-%d").build();
        executor = Executors.newFixedThreadPool(localConfig.getThreadPoolSize(), threadFactory);
    }

    @Override
    public void subscribe(String topic, MessageListener listener) {
        listeners.computeIfAbsent(topic, k -> listener);
    }

    @Override
    public void unsubscribe(String topic) {
        listeners.remove(topic);
    }

    @Override
    public void publish(String topic, String message) {
        MessageListener messageListener = listeners.get(topic);
        if (messageListener != null) {
            executor.submit(() -> messageListener.onMessage(topic, message));
        }
    }
}
