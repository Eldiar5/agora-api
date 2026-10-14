package agora.service.impl;

import agora.dto.response.NotificationDto;
import agora.entity.notification.Notification;
import agora.repository.NotificationRepository;
import agora.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    @Transactional
    @Override
    public void sendNotification(Long recipientId, String recipientLogin, NotificationDto dto) {
        try {
            // 1. Сохраняем в БД (Историю)
            Notification entity = Notification.builder()
                    .recipientId(recipientId)
                    .senderLogin(dto.getSenderLogin())
                    .senderAvatarUrl(dto.getSenderAvatarUrl())
                    .targetId(dto.getTargetId())
                    .message(dto.getMessage())
                    .type(dto.getType())
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            Notification saved = notificationRepository.save(entity);

            // 2. Обогащаем DTO реальным ID из базы и статусом
            dto.setId(saved.getId()); // Важно для фронта, чтобы потом пометить как прочитанное
            dto.setRead(false);
            dto.setTimestamp(entity.getCreatedAt());

            // 3. В WebSocket отправляем по ЛОГИНУ (для доставки здесь и сейчас)
            messagingTemplate.convertAndSendToUser(
                    recipientLogin,
                    "/queue/notifications",
                    dto
            );

            log.info("🔥 Уведомление сохранено и отправлено пользователю {}", recipientId);

        } catch (Exception e) {
            log.error("Ошибка при обработке уведомления", e);
        }
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        // Можно добавить пагинацию, но пока берем просто список
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);
    }
}
