package com.jdragon.message.local;

import lombok.Data;

@Data
public class LocalConfig {

    private int threadPoolSize = 4;

    private String poolNamePrefix = "websocket-executor-";
}
