package agora.mapper.v1;

import org.springframework.stereotype.Component;
import agora.entity.post.Post;
import agora.entity.post.PostJpaEntity;
import agora.entity.tags.Tags;
import agora.exceptions.AgoraIllegalArgumentException;

@Component
public class PostJpaMapper {

    public PostJpaMapper() {}

    /**
     * Преобразует доменный объект Post в JPA-сущность PostJpaEntity.
     * @param domainPost доменный объект
     * @return JPA-сущность, готовая к сохранению в БД
     */

    public PostJpaEntity toEntity(Post domainPost) {
        if (domainPost == null) {
            throw new AgoraIllegalArgumentException("Domain Post is null");
        }

        PostJpaEntity entity = new PostJpaEntity();

        // Преобразуем простые поля
        entity.setPostId(domainPost.getPostId() != null ? domainPost.getPostId() : null);
        entity.setTopic(domainPost.getTopic());
        entity.setText(domainPost.getText());
        entity.setCreatedAt(domainPost.getCreatedAt());
        entity.setAuthor(domainPost.getAuthor());

        entity.setImageUrl(domainPost.getImageUrl());

        entity.setLikesCount(domainPost.getLikesCount() != null ? domainPost.getLikesCount() : 0L);
        entity.setCommentsCount(domainPost.getCommentsCount() != null ? domainPost.getCommentsCount() : 0L);

        return entity;
    }

    /**
     * Преобразует JPA-сущность PostJpaEntity в доменный объект Post.
     * @param entity JPA-сущность из базы данных
     * @return "чистый" доменный объект
     */
    public Post toDomain(PostJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Post domainPost = new Post();
        domainPost.setPostId(entity.getPostId() != null ? entity.getPostId() : null);

        if (entity.getAuthor() != null && entity.getAuthor().getId() != null) {
            domainPost.setAuthor(entity.getAuthor());
        }

        domainPost.setTopic(entity.getTopic());
        domainPost.setText(entity.getText());
        domainPost.setCreatedAt(entity.getCreatedAt());

        domainPost.setImageUrl(entity.getImageUrl());

        domainPost.setLikesCount(entity.getLikesCount() != null ? entity.getLikesCount() : 0L);
        domainPost.setCommentsCount(entity.getCommentsCount() != null ? entity.getCommentsCount() : 0L);

        if (entity.getTags() != null) {
            domainPost.setTags(entity.getTags().stream()
                    .map(Tags::getTagName)
                    .toArray(String[]::new));
        } else {
            domainPost.setTags(new String[0]);
        }

        return domainPost;
    }
}
