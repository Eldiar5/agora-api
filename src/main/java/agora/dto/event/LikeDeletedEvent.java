package agora.dto.event;

import java.time.Instant;

public record LikeDeletedEvent(
        Long postId,
        String whoUnliked, // Кто убрал лайк
        Instant deletedAt
) {}
