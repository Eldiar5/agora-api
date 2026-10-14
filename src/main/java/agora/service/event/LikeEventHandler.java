package agora.service.event;

import agora.dto.response.NotificationDto;
import agora.entity.post.PostJpaEntity;
import agora.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import agora.dto.event.LikeCreatedEvent;
import agora.dto.event.LikeDeletedEvent;
import agora.repository.PostRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@RabbitListener(queues = "${rabbitmq.queue.api-likes:api.like.created.queue}")
public class LikeEventHandler {

    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @RabbitHandler
    @Transactional
    public void handleLikeCreated(LikeCreatedEvent event) {
        log.info("📥 [API] Получено событие лайка для поста ID: {}", event.postId());

        postRepository.findById(event.postId()).ifPresentOrElse(
                post -> processLikeCreation(post, event),
                () -> log.warn("⚠️ [API] Пост с ID {} не найден, лайк проигнорирован", event.postId())
        );
    }

    private void processLikeCreation(PostJpaEntity post, LikeCreatedEvent event) {
        post.setLikesCount(post.getLikesCount() + 1);
        log.debug("✅ [API] Счетчик лайков увеличен. Пост: {}, Всего: {}", post.getPostId(), post.getLikesCount());

        String postAuthorLogin = post.getAuthor().getLogin();
        String likerLogin = event.whoLiked();

        if (postAuthorLogin.equals(likerLogin)) {
            return; // Не спамим уведомлениями самому себе
        }

        NotificationDto notification = NotificationDto.builder()
                .message(String.format("Пользователю %s нравится ваша публикация", likerLogin))
                .senderLogin(likerLogin)
                .senderAvatarUrl(event.likerAvatarUrl())
                .targetId(post.getPostId())
                .type("LIKE")
                .build();

        notificationService.sendNotification(post.getAuthor().getId(), postAuthorLogin, notification);
        log.info("🔔 [API] Уведомление отправлено пользователю: {}", postAuthorLogin);
    }

    @RabbitHandler
    @Transactional
    public void handleLikeDeleted(LikeDeletedEvent event) {
        log.info("🗑️ [API] Лайк убран с поста ID: {}", event.postId());

        postRepository.findById(event.postId()).ifPresent(post -> {
            long newCount = Math.max(0, post.getLikesCount() - 1);
            post.setLikesCount(newCount);
            postRepository.save(post);
            log.info("mV [API] Лайк удален. Счетчик: {}", newCount);

            // Обычно уведомления об удалении лайка не шлют, чтобы не спамить
        });
    }
}