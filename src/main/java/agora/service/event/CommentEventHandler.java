package agora.service.event;

import agora.dto.event.CommentCreatedEvent;
import agora.dto.event.CommentDeletedEvent;
import agora.dto.response.NotificationDto;
import agora.repository.PostRepository;
import agora.service.impl.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@RabbitListener(queues = "${rabbitmq.queue.api-comments:agora.api.comments.queue}")
public class CommentEventHandler {

    private final PostRepository postRepository;
    private final NotificationServiceImpl notificationService;

    @RabbitHandler
    @Transactional
    public void handleCreated(CommentCreatedEvent event) {
        log.info("📥 [API] Комментарий создан к посту ID: {}", event.getPostId());

        postRepository.findById(event.getPostId()).ifPresent(post -> {
            String postAuthor = post.getAuthor().getLogin();
            Long authorId = post.getAuthor().getId();

            String commenter = event.getAuthorLogin();

            if (!postAuthor.equals(commenter)) {
                NotificationDto notification = NotificationDto.builder()
                        .type("COMMENT")
                        .message("@" + commenter + " прокомментировал ваш пост")
                        .senderLogin(commenter)
                        .senderAvatarUrl(event.getCommenterAvatarUrl())
                        .targetId(post.getPostId())
                        .build();

                notificationService.sendNotification(authorId, postAuthor, notification);
            }
        });

        postRepository.incrementCommentsCount(event.getPostId());
    }

    @RabbitHandler
    @Transactional
    public void handleDeleted(CommentDeletedEvent event) {
        log.info("🗑️ [API] Комментарий удален у поста ID: {}", event.getPostId());
        postRepository.decrementCommentsCount(event.getPostId());
    }
}