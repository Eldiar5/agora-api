package agora.service;

import agora.dto.response.NotificationDto;
import agora.entity.notification.Notification;

import java.util.List;

public interface NotificationService {

    // Сохраняет уведомление в БД и отправляет в WebSocket
    void sendNotification(Long recipientId, String recipientLogin, NotificationDto dto);

    // Получить историю уведомлений (для заполнения списка при загрузке)
    List<Notification> getUserNotifications(Long userId);

    // Прочитать уведомления (погасить факел)
    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

}
