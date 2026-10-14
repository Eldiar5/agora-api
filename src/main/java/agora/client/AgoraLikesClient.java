package agora.client;

import feign.Headers;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
//url = "${feign.agora.likes.service.url}",
@FeignClient(name = "AGORA-LIKES",  path = "/api/v1/likes")
@Headers("Content-Type: application/json")
public interface AgoraLikesClient {

    @PostMapping("/check")
    List<Long> getLikedPostIds(
            @RequestBody List<Long> postIds,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    );

}
