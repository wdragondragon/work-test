package com.jdragon.websocket.core;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSoecketSessionManager {
    private static final Logger logger = LoggerFactory.getLogger(WebSoecketSessionManager.class);
    static public final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    static public final ConcurrentHashMap<String, String> sessionIds = new ConcurrentHashMap<>();

    public void addSession(String token, Session session) {
        sessions.put(token, session);
        sessionIds.put(session.getId(), token);
    }

    public String getToken(String sessionId) {
        return sessionIds.get(sessionId);
    }

    public void removeSession(String token) {
        sessions.remove(token);
    }

    public void removeSession(Session session) {
        sessions.values().remove(session);
    }

    public Session getSession(String token) {
        return sessions.get(token);
    }

    public void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            logger.error("Error sending message to session: {}", session.getId(), e);
        }
    }

    public Set<String> connectToken() {
        return sessions.keySet();
    }
}