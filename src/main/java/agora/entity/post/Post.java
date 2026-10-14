package agora.entity.post;

import jakarta.persistence.Column;
import lombok.Data;
import agora.entity.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Post {

    private User author;
    private Long postId;

    private String topic;
    private String text;
    private String[] tags;
    private String imageUrl;

    private LocalDateTime createdAt;

    private Long likesCount = 0L;
    private Long commentsCount = 0L;

    public boolean hasTag(String tag) {

        if (tags == null) {
            return false;
        }

        for(String t : this.tags) {
            if(tag.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public String toFile() {
        DateTimeFormatter fCreatedAt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAtString = this.createdAt.format(fCreatedAt);
        return String.format("[POST]{%d}{%d}{%s}{%s}{%s}{%s}",
                this.author.getId(), this.postId, this.topic, this.text, String.join(",", this.tags), createdAtString);
    }
}
