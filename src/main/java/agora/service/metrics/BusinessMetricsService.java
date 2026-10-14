package agora.service.metrics;

import agora.repository.PostRepository;
import agora.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class BusinessMetricsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final MeterRegistry meterRegistry;

    private final AtomicLong totalUsers = new AtomicLong(0);
    private final AtomicLong totalPosts = new AtomicLong(0);

    public BusinessMetricsService(UserRepository userRepository, PostRepository postRepository, MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.meterRegistry = meterRegistry;

        this.meterRegistry.gauge("agora.business.users.total", totalUsers);
        this.meterRegistry.gauge("agora.business.posts.total", totalPosts);
    }

    @PostConstruct
    public void init() {
        log.info("Инициализация бизнес-метрик из БД...");
        // Делаем SELECT COUNT(*) только один раз при старте микросервиса
        totalUsers.set(userRepository.count());
        totalPosts.set(postRepository.count());
        log.info("Метрики загружены: пользователей = {}, постов = {}", totalUsers.get(), totalPosts.get());
    }

    public void incrementUsers() {
        totalUsers.incrementAndGet();
    }

    public void incrementPosts() {
        totalPosts.incrementAndGet();
    }

    public void decrementPosts() {
        totalPosts.decrementAndGet();
    }

}
