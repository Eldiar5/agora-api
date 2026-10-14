package agora.dto.event;

import java.time.Instant;

public record LikeCreatedEvent(
        Long postId,
        String whoLiked,
        String likerAvatarUrl,
        Long postAuthorId, // Чей пост лайкнули (полезно для уведомлений автору)
        Instant createdAt
) {}
