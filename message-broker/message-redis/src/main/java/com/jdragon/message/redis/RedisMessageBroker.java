package com.jdragon.message.redis;


import com.jdragon.message.core.MessageBroker;
import com.jdragon.message.core.MessageListener;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisMessageBroker implements MessageBroker {

    private final RedisManager redisManager;

    public RedisMessageBroker(RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    @Override
    public void subscribe(String topic, MessageListener listener) {
        if (redisManager.isSubscribed(topic)) {
            return;
        }
        CountDownLatch subscribedLatch = new CountDownLatch(1);
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                new Thread(() -> listener.onMessage(channel, message)).start();
                log.info("Received message: {} from channel: {}", message, channel);
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                log.info("Subscribed to channel: {}", channel);
                subscribedLatch.countDown();
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                log.info("Unsubscribed from channel: {}", channel);
            }
        };
        JedisPubSub pubSubListener = redisManager.createPubSubListener(topic, jedisPubSub);
        redisManager.subscribeChannel(topic, pubSubListener);
        try {
            // 等待订阅成功，最长等待5秒
            if (!subscribedLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("订阅超时，Redis监听器未启动");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void unsubscribe(String topic) {
        redisManager.unsubscribeChannel(topic);
    }

    @Override
    public void publish(String topic, String message) {
        redisManager.publishMessage(topic, message);
    }
}
