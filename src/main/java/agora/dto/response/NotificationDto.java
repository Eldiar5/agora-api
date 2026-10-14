package agora.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {

    private Long id;
    private String type;

    private String message;
    private String senderLogin;

    private String senderAvatarUrl;
    private Long targetId;

    private boolean read;
    private LocalDateTime timestamp;

}
