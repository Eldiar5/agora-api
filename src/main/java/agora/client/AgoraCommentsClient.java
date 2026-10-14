package agora.client;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import agora.dto.response.CommentResponseDto;

import java.util.List;
import java.util.Map;
//url = "${feign.agora.comments.service.url}",
@FeignClient(name = "AGORA-COMMENTS",  path = "/api/v1")
@Headers("Content-Type: application/json")
public interface AgoraCommentsClient {

    // --- InfoController ---
    @GetMapping("/comments/{id}")
    CommentResponseDto getCommentById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token

    );

    // --- PostController ---
    @GetMapping("/comments/post/{postId}")
    List<CommentResponseDto> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String token

    );

    // Новый метод: Принимает список ID постов, возвращает Map<ID Поста, Список Комментариев>
    @PostMapping("/comments/batch")
    Map<Long, List<CommentResponseDto>> getCommentsBatch(
            @RequestBody List<Long> postIds,
            @RequestHeader("Authorization") String token

    );
}
