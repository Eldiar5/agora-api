package agora.mapper.v2.impl;

import org.springframework.stereotype.Component;
import agora.dto.response.CommentResponseDto;
import agora.dto.response.PostResponseDto;
import agora.entity.post.Post;
import agora.entity.user.User;
import agora.mapper.v2.HttpPostMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class HttpPostMapperImpl implements HttpPostMapper {

    public HttpPostMapperImpl() {}

    @Override
    public PostResponseDto mapPostToResponseDto(Post post, User author, List<CommentResponseDto> comments, boolean isLiked) {
        PostResponseDto postResponseDto = new PostResponseDto();

        postResponseDto.setAuthorLogin(author.getLogin());
        postResponseDto.setPostId(post.getPostId());
        postResponseDto.setAuthorAvatarUrl(author.getAvatarUrl());

        postResponseDto.setTopic(post.getTopic());
        postResponseDto.setText(post.getText());
        postResponseDto.setTags(Arrays.asList(post.getTags()));

        postResponseDto.setImageUrl(post.getImageUrl());

        postResponseDto.setLikesCount(post.getLikesCount());
        postResponseDto.setComments(comments != null ? comments : Collections.emptyList());

        postResponseDto.setCommentsCount(post.getCommentsCount());

        postResponseDto.setCreatedAt(post.getCreatedAt());
        postResponseDto.setLiked(isLiked);

        return postResponseDto;
    }
}
