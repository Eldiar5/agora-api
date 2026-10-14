package agora.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {

    private Long id;
    private String text;
    private Long postId;
    private String authorLogin;
    private LocalDateTime createdAt;

}
