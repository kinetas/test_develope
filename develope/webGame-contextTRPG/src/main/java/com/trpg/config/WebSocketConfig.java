package com.trpg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket configuration.
 *
 * <ul>
 *   <li>Handshake endpoint : {@code /ws} (SockJS fallback enabled)</li>
 *   <li>Broadcast prefix   : {@code /topic}  — fan-out to all subscribers</li>
 *   <li>Point-to-point     : {@code /queue}  — unicast messages</li>
 *   <li>App send prefix    : {@code /app}    — routes to @MessageMapping methods</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Allow connections from any origin during development.
                // Restrict to specific domains in production.
                .setAllowedOriginPatterns("*")
                .withSockJS();          // SockJS fallback for browsers without native WS
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory broker on /topic and /queue
        registry.enableSimpleBroker("/topic", "/queue");

        // Messages with destination prefix /app are routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }
}
