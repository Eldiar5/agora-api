package agora.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreatedEvent {

    private Long commentId;
    private Long postId;
    private String authorLogin;
    private String commenterAvatarUrl;

}
