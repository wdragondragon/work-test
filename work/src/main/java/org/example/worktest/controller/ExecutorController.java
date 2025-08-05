package org.example.worktest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jdragon.message.core.MessageBroker;
import org.example.worktest.service.impl.TestWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/test")
    public String test(String message) {
        // 添加具体的业务逻辑
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String token = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
        messageBroker.publish(token, "test msg");
        return "test"; // 返回视图名称或数据
    }

    @GetMapping("/join")
    public String join(String message) {
        // 添加具体的业务逻辑
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String token = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
        testWS.join(token);
        return "test"; // 返回视图名称或数据
    }

    @GetMapping("/exit")
    public String exit(String message) {
        // 添加具体的业务逻辑
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String token = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
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
