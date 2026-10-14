package agora.mapper.v2;

import agora.dto.response.CommentResponseDto;
import agora.dto.response.PostResponseDto;
import agora.entity.post.Post;
import agora.entity.user.User;

import java.util.List;

public interface HttpPostMapper {

    PostResponseDto mapPostToResponseDto(Post post, User author, List<CommentResponseDto> comments, boolean isLiked);

}
