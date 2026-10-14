package agora.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import agora.entity.subscription.Subscription;
import agora.entity.user.User;
import agora.exceptions.AgoraIllegalArgumentException;
import agora.repository.SubscriptionRepository;
import agora.service.SubscriptionService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void subscribe(User follower, User following) {
        if (follower.equals(following)) {
            throw new AgoraIllegalArgumentException("Нельзя подписаться на самого себя");
        }
        if (subscriptionRepository.existsByFollowerAndFollowing(follower, following)) {
            return; // Уже подписан (идемпотентность)
        }

        Subscription subscription = new Subscription();
        subscription.setFollower(follower);
        subscription.setFollowing(following);
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(User follower, User following) {
        subscriptionRepository.deleteByFollowerAndFollowing(follower, following);
    }

    @Override
    public boolean isFollowing(User follower, User following) {
        return subscriptionRepository.existsByFollowerAndFollowing(follower, following);
    }

    @Override
    public Long getFollowersCount(User user) {
        return subscriptionRepository.countByFollowing(user);
    }

    @Override
    public Long getFollowingCount(User user) {
        return subscriptionRepository.countByFollower(user);
    }
}
