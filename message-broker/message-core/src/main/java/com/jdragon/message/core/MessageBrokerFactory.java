package com.jdragon.message.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

public class MessageBrokerFactory {
    private static final List<MessageBrokerProvider> providers = loadProviders();

    private static List<MessageBrokerProvider> loadProviders() {
        ServiceLoader<MessageBrokerProvider> loader = ServiceLoader.load(MessageBrokerProvider.class);
        List<MessageBrokerProvider> result = new ArrayList<>();
        loader.iterator().forEachRemaining(result::add);
        return result;
    }

    public static MessageBroker create(MessageBrokerProperties config) {
        for (MessageBrokerProvider provider : providers) {
            if (Objects.equals(provider.getType(), config.getType())) {
                return provider.createMessageBroker(config.getConfig());
            }
        }
        throw new IllegalArgumentException("No MessageBrokerProvider found for type: " + config.getType());
    }
}
