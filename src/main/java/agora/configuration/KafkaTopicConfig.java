package agora.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic postsTopic() {
        return TopicBuilder.name("agora.posts")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // JsonSerializer по умолчанию берёт свой ObjectMapper (даты пишет массивом [2026,10,19,...]).
    // Подменяем на ObjectMapper Spring Boot: JavaTimeModule + WRITE_DATES_AS_TIMESTAMPS=false → ISO-строки.
    // Свойства spring.json.* (type.mapping) применяются и к этому экземпляру: фабрика вызывает configure().
    @Bean
    @SuppressWarnings("unchecked")
    public DefaultKafkaProducerFactoryCustomizer jsonSerializerCustomizer(ObjectMapper objectMapper) {
        return producerFactory -> ((DefaultKafkaProducerFactory<Object, Object>) producerFactory)
                .setValueSerializerSupplier(() -> new JsonSerializer<>(objectMapper));
    }

}
