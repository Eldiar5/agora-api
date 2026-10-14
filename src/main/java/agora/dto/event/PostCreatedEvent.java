package agora.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreatedEvent {
    private Long postId;
    private String authorUsername;
    private LocalDateTime createdAt;
}
