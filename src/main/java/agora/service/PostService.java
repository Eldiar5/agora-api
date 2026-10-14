package agora.service;

import agora.entity.post.Post;
import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.exceptions.PostNotFoundException;
import agora.exceptions.UserNotFoundException;

import java.util.List;

public interface PostService {

    Post savePost(Post post) throws PostNotFoundException;

    List<Post> getPostsByUser(User user) throws PostNotFoundException;

    List<Post> getAllPosts() throws PostNotFoundException;

    List<Post> getPostsByTag(String tag) throws PostNotFoundException;

    List<Post> getPostsByUserType(UserType userType) throws PostNotFoundException, UserNotFoundException;

    Post getPostById(Long postId) throws PostNotFoundException;
    void deletePost(Post post) throws PostNotFoundException;

    List<Post> getFeedPosts(User user);

}
