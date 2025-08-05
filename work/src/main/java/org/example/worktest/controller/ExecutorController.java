package org.example.worktest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jdragon.message.core.MessageBroker;
import com.jdragon.websocket.annotation.WsMapping;
import com.jdragon.websocket.annotation.WsParam;
import com.jdragon.websocket.annotation.WsRoute;
import com.jdragon.websocket.model.Message;
import org.example.worktest.entity.Order;
import org.example.worktest.service.impl.TestWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@WsRoute
@RequestMapping("/api/executor")
@RestController
public class ExecutorController {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorController.class);

    private final MessageBroker messageBroker;

    private final TestWS testWS;

    public ExecutorController(MessageBroker messageBroker, TestWS testWS) {
        this.messageBroker = messageBroker;
        this.testWS = testWS;
    }


    @WsMapping
    @GetMapping("/test")
    public String test(@WsParam("token") String token, Message<Order> message, Order order) {
        messageBroker.publish(token, token + " test msg: " + message);
        messageBroker.publish(message.getToken(), "test msg 2" + order);
        return "test"; // 返回视图名称或数据
    }

    @WsMapping
    @GetMapping("/join")
    public String join(@WsParam("token") String token) {
        testWS.join(token);
        return "test"; // 返回视图名称或数据
    }

    @WsMapping
    @GetMapping("/exit")
    public String exit(@WsParam("token") String token) {
        testWS.exit(token);
        return "test"; // 返回视图名称或数据
    }


    @GetMapping("/subscribe")
    public String subscribe(String message) {
        // 添加具体的业务逻辑
        logger.info("--------------subscribe message: {}", message);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String token = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
        // 发送订阅成功消息
        ObjectNode subscribedResponse = objectMapper.createObjectNode();
        subscribedResponse.put("action", "subscribed");
        subscribedResponse.put("token", token);

        try {
            messageBroker.publish(token, objectMapper.writeValueAsString(subscribedResponse));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return "subscribe"; // 返回视图名称或数据
    }

    @GetMapping("/unsubscribe")
    public String unsubscribe(String message) {
        // 添加具体的业务逻辑
        logger.info("--------------unsubscribe message: {}", message);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String token = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
        // 发送取消订阅redis消息
        if (token != null && !token.isEmpty()) {
            ObjectNode unsubscribedResponse = objectMapper.createObjectNode();
            unsubscribedResponse.put("action", "unsubscribed");
            unsubscribedResponse.put("token", token);
            messageBroker.unsubscribe(token);
            try {
                messageBroker.publish(token, objectMapper.writeValueAsString(unsubscribedResponse));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return "unsubscribe"; // 返回视图名称或数据
    }
}
