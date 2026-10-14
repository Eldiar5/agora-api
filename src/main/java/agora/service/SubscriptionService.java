package agora.service;

import agora.entity.user.User;

public interface SubscriptionService {

    public void subscribe(User follower, User following);

    public void unsubscribe(User follower, User following);

    public boolean isFollowing(User follower, User following);

    public Long getFollowersCount(User user);
    public Long getFollowingCount(User user);

}
