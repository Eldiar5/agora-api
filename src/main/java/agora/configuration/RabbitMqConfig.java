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
public class RabbitMqConfig {

    // Имя очереди ДЛЯ ЭТОГО сервиса (уникальное!)
    @Value("${rabbitmq.queue.api-likes}")
    private String queueName;

    // Имя Exchange (ДОЛЖНО СОВПАДАТЬ с тем, что в agora-likes)
    @Value("${rabbitmq.exchange.likes:likes.exchange}")
    private String exchangeName;

    // Routing Key (ДОЛЖЕН СОВПАДАТЬ с тем, что шлет agora-likes)
    @Value("${rabbitmq.routing.key.like-created:like.created.key}")
    private String createRoutingKey;

    @Value("${rabbitmq.routing.key.like-deleted:like.deleted.key}")
    private String deleteRoutingKey;

    // === НОВЫЕ НАСТРОЙКИ ДЛЯ КОММЕНТАРИЕВ ===
    @Value("${rabbitmq.queue.api-comments:api.comment.created.queue}") // Имя очереди
    private String commentsQueueName;

    @Value("${rabbitmq.exchange.comments:comments.exchange}") // Имя обменника (как в agora-comments)
    private String commentsExchangeName;

    @Value("${rabbitmq.routing.key.comment-created:comment.created.key}") // Ключ (как в agora-comments)
    private String commentCreatedKey;

    // ---- бины для лайков ---- //

    // 1. Объявляем очередь
    @Bean
    public Queue apiLikesQueue() {
        return new Queue(queueName, true);
    }

    // 2. Объявляем Exchange (он уже есть, но объявить не вредно)
    @Bean
    public TopicExchange likesExchange() {
        return new TopicExchange(exchangeName);
    }

    // 3. Связываем очередь с Exchange
    @Bean
    public Binding bindingLikeCreated(Queue apiLikesQueue, TopicExchange likesExchange) {
        return BindingBuilder.bind(apiLikesQueue)
                .to(likesExchange)
                .with(createRoutingKey);
    }

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
    public Binding bindingLikeDeleted(Queue apiLikesQueue, TopicExchange likesExchange) {
        return BindingBuilder.bind(apiLikesQueue)
                .to(likesExchange)
                .with(deleteRoutingKey);
    }

    @Bean
    public TopicExchange internalExchange() {
        return new TopicExchange("internal.exchange");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");

        // 🔥 САМОЕ ВАЖНОЕ: Маппинг имен классов
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        // Ключ - это то, что приходит в заголовке от agora-likes (смотри в логи!)
        // Значение - это твой класс в agora-api
        idClassMapping.put("org.example.agoralikes.dto.event.LikeCreatedEvent", agora.dto.event.LikeCreatedEvent.class);
        idClassMapping.put("org.example.agoralikes.dto.event.LikeDeletedEvent", agora.dto.event.LikeDeletedEvent.class);

        // Добавляем маппинг для комментариев
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
