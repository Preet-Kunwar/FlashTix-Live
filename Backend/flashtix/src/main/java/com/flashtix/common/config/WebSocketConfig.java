package com.flashtix.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint React will use to connect to WebSockets
        registry.addEndpoint("/ws-ticketing")
                .setAllowedOriginPatterns("*") // Allows cross-origin requests from React
                .withSockJS(); // Fallback for browsers that don't support WebSockets
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Clients will subscribe to paths starting with "/topic" to get live updates
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}