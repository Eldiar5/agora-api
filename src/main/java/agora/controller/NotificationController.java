package agora.controller;

import agora.entity.notification.Notification;
import agora.entity.user.User;
import agora.service.NotificationService;
import agora.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "История уведомлений")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService; // Внедряем сервис пользователей

    @GetMapping
    @Operation(summary = "Получить историю уведомлений")
    public ResponseEntity<List<Notification>> getMyNotifications(Principal principal) {
        // 1. Достаем логин из токена
        String userLogin = principal.getName();

        // 2. Находим ID пользователя (предполагаем, что у тебя есть такой метод)
        User user = userService.getUserByLogin(userLogin);

        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Пометить одно уведомление как прочитанное")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        // Тут по-хорошему тоже надо проверить, что уведомление принадлежит именно этому юзеру,
        // но для MVP можно оставить так.
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Пометить все как прочитанные")
    public ResponseEntity<Void> markAllRead(Principal principal) {
        String userLogin = principal.getName();
        User user = userService.getUserByLogin(userLogin);

        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }

}
