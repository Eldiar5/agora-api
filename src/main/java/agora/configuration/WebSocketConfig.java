package agora.configuration;

import agora.security.JwtHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${rabbitmq.stomp.host}")
    private String stompHost;

    @Value("${rabbitmq.stomp.port}")
    private int stompPort;

    private final JwtHandler jwtHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic - для общих рассылок (всем)
        // /queue - для личных сообщений (конкретному юзеру)
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(stompHost)
                .setRelayPort(stompPort)
                .setClientLogin("guest")
                .setClientPasscode("guest")
                .setSystemLogin("guest")
                .setSystemPasscode("guest");

        // Префикс для сообщений, которые клиент отправляет на сервер (нам пока не нужно, но пусть будет)
        config.setApplicationDestinationPrefixes("/app");

        // Префикс для личных сообщений (Spring Security превратит это в /user/{username}/queue/...)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Точка подключения (handshake)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Разрешаем CORS (важно для фронта)
                .withSockJS(); // Включаем SockJS (фоллбэк, если WS не поддерживается)
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Мы просто проверяем, что команда существует и это CONNECT
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // 1. Достаем заголовок Authorization
                    List<String> authorization = accessor.getNativeHeader("Authorization");

                    if (authorization != null && !authorization.isEmpty()) {
                        String token = authorization.getFirst();
                        if (token.startsWith("Bearer ")) {
                            token = token.substring(7);

                            try {
                                // 2. Валидируем токен
                                String username = jwtHandler.getUsernameFromToken(token);

                                // 3. Создаем Authentication
                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(username, null, List.of());

                                // 4. Привязываем юзера к сессии
                                accessor.setUser(auth);
                                log.info("✅ WebSocket Authenticated user: {}", username);

                            } catch (Exception e) {
                                log.error("❌ WebSocket Authentication failed: {}", e.getMessage());
                                return null;
                            }
                        }
                    }
                }
                return message;
            }
        });
    }

}
