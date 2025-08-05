package org.example.worktest.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@Slf4j
@ServerEndpoint("/ws2")
@Component
public class TestWs {
    @OnOpen
    public void onOpen(Session session) {
        log.info("New WebSocket connection established: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("Received message: {}", message);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket connection closed: {}", session.getId());
    }
}
