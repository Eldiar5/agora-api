package agora.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
// Лайки и посты переехали в Kafka (LikeEventHandler, PostServiceImpl).
// Здесь остались только комментарии; STOMP relay для WebSocket настраивается в WebSocketConfig.
public class RabbitMqConfig {

    // === НАСТРОЙКИ ДЛЯ КОММЕНТАРИЕВ ===
    @Value("${rabbitmq.queue.api-comments:api.comment.created.queue}") // Имя очереди
    private String commentsQueueName;

    @Value("${rabbitmq.exchange.comments:comments.exchange}") // Имя обменника (как в agora-comments)
    private String commentsExchangeName;

    @Value("${rabbitmq.routing.key.comment-created:comment.created.key}") // Ключ (как в agora-comments)
    private String commentCreatedKey;

    // ---- бины для комментариев ---- //

    @Bean
    public Queue apiCommentsQueue() {
        return new Queue(commentsQueueName, true);
    }

    // 2. Создаем (или используем существующий) TopicExchange для комментариев
    @Bean
    public TopicExchange commentsExchange() {
        return new TopicExchange(commentsExchangeName);
    }

    // 3. Связываем очередь с обменником
    @Bean
    public Binding bindingCommentCreated() {
        return BindingBuilder.bind(apiCommentsQueue())
                .to(commentsExchange())
                .with(commentCreatedKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");

        // 🔥 САМОЕ ВАЖНОЕ: Маппинг имен классов
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        // Маппинг для комментариев
        // Ключ: полное имя класса-отправителя (из agora-comments)
        // Значение: класс-получатель (который создадим ниже)
        idClassMapping.put("org.example.agoracomments.dto.event.CommentCreatedEvent", agora.dto.event.CommentCreatedEvent.class);
        idClassMapping.put("org.example.agoracomments.dto.event.CommentDeletedEvent", agora.dto.event.CommentDeletedEvent.class);

        typeMapper.setIdClassMapping(idClassMapping);
        // Подключаем маппер к конвертеру
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    // ==========================================
    // 3. ФАБРИКА СЛУШАТЕЛЕЙ
    // ==========================================
    @Bean("jsonContainerFactory") // <-- Даем уникальное имя
    public SimpleRabbitListenerContainerFactory jsonContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter()); // Подключаем конвертер
        return factory;
    }

}
