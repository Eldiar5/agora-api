package agora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import agora.entity.user.User;
import agora.exceptions.UserNotFoundException;
import agora.security.JwtHandler;
import agora.service.SubscriptionService;
import agora.service.UserService;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription API", description = "Управление подписками")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final JwtHandler jwtHandler;

    @PostMapping("/subscribe/{followingId}")
    @Operation(summary = "Подписаться на пользователя")
    public void subscribe(
            @RequestHeader("Authorization") String token,
            @PathVariable Long followingId
    ) {
        User follower = this.getUserFromToken(token);
        User following = userService.getUserById(followingId);
        if (following == null) throw new UserNotFoundException("Пользователь не найден");

        subscriptionService.subscribe(follower, following);
    }

    @PostMapping("/unsubscribe/{followingId}")
    @Operation(summary = "Отписаться от пользователя")
    public void unsubscribe(
            @RequestHeader("Authorization") String token,
            @PathVariable Long followingId
    ) {
        User follower = this.getUserFromToken(token);
        User following = userService.getUserById(followingId);
        if (following == null) throw new UserNotFoundException("Пользователь не найден");

        subscriptionService.unsubscribe(follower, following);
    }

    @GetMapping("/check/{targetId}")
    @Operation(summary = "Проверить подписку", description = "Возвращает true, если текущий пользователь подписан на targetId")
    public boolean isFollowing(
            @RequestHeader("Authorization") String token,
            @PathVariable Long targetId
    ) {
        User follower = this.getUserFromToken(token);
        User target = userService.getUserById(targetId);
        return subscriptionService.isFollowing(follower, target);
    }

    private User getUserFromToken(String token) {
        String jwt = token.substring(7);
        String login = jwtHandler.getUsernameFromToken(jwt);
        return userService.getUserByLogin(login);
    }
}
