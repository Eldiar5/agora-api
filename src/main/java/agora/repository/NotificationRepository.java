package agora.repository;

import agora.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Найти все уведомления пользователя, отсортированные от новых к старым
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipientId = :recipientId AND n.read = false")
    void markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);


    // (Опционально) Посчитать сколько непрочитанных, чтобы зажечь факел без загрузки списка
    long countByRecipientIdAndReadFalse(Long recipientId);

}
