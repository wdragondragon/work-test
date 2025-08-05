package com.jdragon.message.redis;

import lombok.Data;

@Data
public class RedisConfig {

    private String host;

    private int port;

    private int maxTotal;

    private int maxIdle;

    private int minIdle;


}