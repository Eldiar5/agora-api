package agora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import agora.entity.post.PostJpaEntity;
import agora.entity.user.User;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostJpaEntity, Long> {

    List<PostJpaEntity> findByAuthorOrderByCreatedAtDesc(User author);

    @Query("select p from PostJpaEntity p join p.tags t where t.tagName = :tag order by p.createdAt desc")
    List<PostJpaEntity> findByTagName(String tag);

    List<PostJpaEntity> findByAuthorInOrderByCreatedAtDesc(List<User> authors);

    @Modifying
    @Query("UPDATE PostJpaEntity p SET p.likesCount = p.likesCount + 1 WHERE p.postId = :postId")
    void incrementLikesCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostJpaEntity p SET p.commentsCount = p.commentsCount + 1 WHERE p.postId = :postId")
    void incrementCommentsCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostJpaEntity p SET p.commentsCount = p.commentsCount - 1 WHERE p.postId = :postId AND p.commentsCount > 0")
    void decrementCommentsCount(@Param("postId") Long postId);

}
