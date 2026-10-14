package agora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import agora.entity.subscription.Subscription;
import agora.entity.user.User;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    // Удаление подписки (возвращает количество удаленных строк)
    Long deleteByFollowerAndFollowing(User follower, User following);

    // Найти всех, на кого я подписан (чтобы собрать ленту)
    @Query("SELECT s.following FROM Subscription s WHERE s.follower = :follower")
    List<User> findFollowingByFollower(@Param("follower") User follower);

    // Подсчет количества (для профиля)
    Long countByFollower(User follower); // Мои подписки
    Long countByFollowing(User following); // Мои подписчики

}
