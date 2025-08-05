package com.jdragon.websocket;

import com.jdragon.message.core.MessageBroker;
import com.jdragon.message.core.MessageBrokerFactory;
import com.jdragon.message.core.MessageBrokerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@ComponentScan
public class WebsocketDispatcherAutoConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public MessageBrokerProperties messageBrokerProperties() {
        return new MessageBrokerProperties();
    }

    @Bean
    public MessageBroker messageBroker(MessageBrokerProperties messageBrokerProperties) {
        return MessageBrokerFactory.create(messageBrokerProperties);
    }
}
