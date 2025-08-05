package com.jdragon.message.redis;

public interface CallBackRunable {
    void run(String channel, String message);
}
