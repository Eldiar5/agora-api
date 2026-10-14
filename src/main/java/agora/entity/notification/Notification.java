package agora.entity.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientId;

    @Column(name = "sender_login", nullable = false)
    private String senderLogin;

    @Column(name = "sender_avatar_url")
    private String senderAvatarUrl;

    @Column(name = "target_id") // Без nullable = false!
    private Long targetId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @CreationTimestamp
    private LocalDateTime createdAt;
}