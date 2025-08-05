package com.jdragon.websocket.model;

import lombok.Data;

@Data
public class Message<T> {

    private String action;

    private String token;

    private T param;

}
