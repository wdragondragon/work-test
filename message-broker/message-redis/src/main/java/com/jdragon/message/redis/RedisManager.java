package com.jdragon.message.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RedisManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisManager.class);

    static public final ConcurrentHashMap<String, JedisPubSub> jedisPubSubs = new ConcurrentHashMap<>();

    private final JedisPool jedisPool;



    public RedisManager(RedisConfig redisConfig) {
        this.jedisPool = jedisPool(redisConfig);
    }

    public JedisPool jedisPool(RedisConfig redisConfig) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfig.getMaxTotal());
        config.setMaxIdle(redisConfig.getMaxIdle());
        config.setMinIdle(redisConfig.getMinIdle());
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        // 禁用JMX避免MBean注册异常
        config.setJmxEnabled(false);
        return new JedisPool(config, redisConfig.getHost(), redisConfig.getPort());
    }

    public void publishMessage(String channel, String message) {
        logger.info("Publishing message to channel( {} ) : {} ", channel, message);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, message);
        } catch (Exception e) {
            // 记录发布异常
            logger.error("Failed to publish message to channel: {}", channel, e);
        }
    }

    public void publishMessage(String message) {
        logger.info("Publishing(broadcast) message : {} ", message);
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> channels = jedis.pubsubChannels();
            for (String channel : channels) {
                jedis.publish(channel, message);
            }
        } catch (Exception e) {
            // 记录发布异常
            logger.error("Failed to broadcast message", e);
        }
    }

    public List<String> getPubsubChannels(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pubsubChannels(pattern);
        } catch (Exception e) {
            // 记录订阅异常
            logger.error("Failed to get pubsub channels with pattern: {}", pattern, e);
        }
        return null;
    }

    public void subscribeChannel(String channel, JedisPubSub listener) {
        if (null == listener) {
            return;
        }
        new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                logger.info("jedis subscribe channel {}", channel);
                jedis.subscribe(listener, channel);
            } catch (Exception e) {
                // 记录订阅异常
                logger.error("Failed to subscribe to channel: {}", channel, e);
            }
            logger.info("jedis subscribe channel {} is end", channel);
        }).start();

    }

    public void psubscribeChannel(String pattern, JedisPubSub listener) {
        if (null == listener) {
            return;
        }
        new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.psubscribe(listener, pattern);
            } catch (Exception e) {
                // 记录订阅异常
                logger.error("Failed to psubscribe to pattern: " + pattern, e);
            }
        }).start();

    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

    public boolean isSubscribed(String channel) {
        return jedisPubSubs.containsKey(channel);
    }

    public JedisPubSub createPubSubListener(String channel, JedisPubSub jedisPubSub) {
        if (jedisPubSubs.containsKey(channel)) {
            return null;
        }
        jedisPubSubs.put(channel, jedisPubSub);
        return jedisPubSub;
    }


    public void unsubscribeChannel(String channel) {
        JedisPubSub jedisPubSub = jedisPubSubs.get(channel);
        if (jedisPubSub != null) {
            try {
                jedisPubSub.unsubscribe(channel);
                jedisPubSubs.remove(channel);
            } catch (Exception e) {
                logger.error("Failed to unsubscribe from channel: {}", channel, e);
            }
        } else {
            logger.warn("No subscriber found for channel: {}", channel);
        }
    }

    public List<String> list(String domainChannelList) {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.lrange(domainChannelList, 0, jedis.llen(domainChannelList));
            logger.info("get domainChannelList: {} result: {}", domainChannelList, result);
            return result;
        } catch (Exception e) {
            logger.error("Failed to get domainChannelList: {}", domainChannelList, e);
        }
        return null;
    }

    public void lpush(String key, String value) {
        // 添加参数校验
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("Failed to lpush value: " + value + " to key: " + key, e);
            throw new RuntimeException("Failed to execute lpush command for key: " + key, e);
        }
    }

}
