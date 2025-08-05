package com.jdragon.websocket.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jdragon.message.core.MessageBroker;
import com.jdragon.websocket.msgcontrol.JobController;
import com.jdragon.websocket.util.SpringContextUtil;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@ServerEndpoint("/ws")
@Component
public class WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    ApplicationContext context = SpringContextUtil.getApplicationContext();

    private WebSoecketSessionManager getWsSessionManager() {
        return context.getBean(WebSoecketSessionManager.class);
    }

    private MessageBroker getMessageBroker() {
        return context.getBean(MessageBroker.class);
    }

    public WebSocketServer() {
        logger.info("websocket server init...");
    }

    @OnOpen
    public void onOpen(Session session) {
        logger.info("New WebSocket connection established: {}", session.getId());
        // 发送欢迎消息
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("action", "connected");
            response.put("sessionId", session.getId());
            getWsSessionManager().sendMessage(session, objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            logger.error("Error sending welcome message", e);
        }
    }

    private JobController getJobController() {
        return context.getBean(JobController.class);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.debug("Received message: {} from session: {}", message, session.getId());
        try {
            if ("PING".equals(message)) {
                getWsSessionManager().sendMessage(session, "PONG");
                return;
            } else if ("PONG".equals(message)) {
                return;
            }
            JsonNode jsonNode = objectMapper.readTree(message);
            String action = jsonNode.get("action").asText();
            String token = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
            switch (action.toLowerCase()) {
                case "ping":
                    ObjectNode pongResponse = objectMapper.createObjectNode();
                    pongResponse.put("action", "pong");
                    getWsSessionManager().sendMessage(session, objectMapper.writeValueAsString(pongResponse));
                    break;
                case "unsubscribe":
                    if (token != null && !token.isEmpty()) {
                        // 取消订阅redis消息
                        ObjectNode unsubscribedResponse = objectMapper.createObjectNode();
                        unsubscribedResponse.put("action", "unsubscribed");
                        unsubscribedResponse.put("token", token);
                        getWsSessionManager().removeSession(token);
                        getMessageBroker().unsubscribe(token);
                        session.getAsyncRemote().sendText(objectMapper.writeValueAsString(unsubscribedResponse));
                        session.close();
                    } else {
                        ObjectNode errorResponse = objectMapper.createObjectNode();
                        errorResponse.put("error", "token is required for unsubscription");
                        getWsSessionManager().sendMessage(session, objectMapper.writeValueAsString(errorResponse));
                    }
                    break;
                default:
                    if (token != null && !token.isEmpty()) {
                        getWsSessionManager().addSession(token, session);
                        getMessageBroker().subscribe(token, (channel, msg) -> {
                            getWsSessionManager().sendMessage(getWsSessionManager().getSession(channel), msg);
                        });
                    } else {
                        ObjectNode errorResponse = objectMapper.createObjectNode();
                        errorResponse.put("error", "token is required for subscription");
                        getWsSessionManager().sendMessage(session, objectMapper.writeValueAsString(errorResponse));
                    }
                    break;
            }
            getJobController().dispatchMessageByAction(message);
        } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
            try {
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("error", "Invalid message format");
                getWsSessionManager().sendMessage(session, objectMapper.writeValueAsString(errorResponse));
            } catch (Exception ex) {
                logger.error("Error sending error message", ex);
            }
        }
    }


    @OnClose
    public void onClose(Session session) {
        logger.info("WebSocket connection closed: {}", session.getId());
        // 清理会话
        String token = getWsSessionManager().getToken(session.getId());
        getWsSessionManager().removeSession(token);
        getMessageBroker().unsubscribe(token);
    }


}
