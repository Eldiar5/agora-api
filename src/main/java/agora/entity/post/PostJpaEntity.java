package agora.entity.post;

import jakarta.persistence.*;
import lombok.Data;
import agora.entity.tags.Tags;
import agora.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "posts")
public class PostJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "likes_count", nullable = false)
    private Long likesCount = 0L;

    @Column(name = "comments_count", nullable = false)
    private Long commentsCount = 0L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;

    @Column(name = "topic")
    private String topic;

    @Column(name = "text")
    private String text;

    @ManyToMany(fetch = FetchType.EAGER,  cascade = {CascadeType.MERGE})
    @JoinTable(name = "post_and_tags", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tags> tags;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
