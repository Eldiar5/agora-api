package agora.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class PostResponseDto {

    private String authorLogin;
    private Long postId;
    private String authorAvatarUrl;

    private String topic;
    private String text;
    private String imageUrl;
    private List<String> tags;

    private Long likesCount;
    private Long commentsCount;

    @JsonProperty("isLiked")
    private boolean isLiked;

    private List<CommentResponseDto> comments;

    private LocalDateTime createdAt;

}
