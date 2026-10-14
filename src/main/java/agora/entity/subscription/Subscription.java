package agora.entity.subscription;

import jakarta.persistence.*;
import lombok.Data;
import agora.entity.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "following_id"}) // Защита от дублей
})
@Data
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower; // Тот, КТО подписывается

    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private User following; // Тот, НА КОГО подписываются

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
