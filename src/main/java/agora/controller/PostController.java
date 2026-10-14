package agora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import agora.client.AgoraCommentsClient;
import agora.client.AgoraLikesClient;
import agora.configuration.swagger.doc.*;
import agora.dto.request.PostRequestDto;
import agora.dto.response.CommentResponseDto;
import agora.dto.response.PostResponseDto;
import agora.entity.post.Post;
import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.exceptions.PostNotFoundException;
import agora.exceptions.AgoraIllegalArgumentException;
import agora.exceptions.UserNotFoundException;
import agora.mapper.v2.HttpPostMapper;
import agora.security.JwtHandler;
import agora.service.PostService;
import agora.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
@Tag(name = "Post API", description = "API для создания, получения и удаления постов (Требует JWT токен)")
@ApiErrorResponse500
@ApiErrorResponse401
@ApiErrorResponse404
@ApiErrorResponse400
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final HttpPostMapper postMapper;
    private final JwtHandler jwtHandler;
    private final AgoraCommentsClient commentsClient;
    private final AgoraLikesClient likesClient;

    @PostMapping("/create")
    @Operation(summary = "Создать новый пост", description = "Публикует новый пост от имени аутентифицированного пользователя")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponseDto addPost(
            @RequestBody @Valid PostRequestDto postRequestDto,
            @RequestHeader(name = "Authorization") String token

    ) {
        String authorization = token.substring(7);

        jwtHandler.validateToken(authorization);
        String author = jwtHandler.getUsernameFromToken(authorization);

        User user = userService.getUserByLogin(author);

        Post post = new Post();

        post.setTopic(postRequestDto.getTopic());
        post.setText(postRequestDto.getText());
        post.setImageUrl(postRequestDto.getImageUrl());

        if (!Objects.isNull(postRequestDto.getTags())) {
            post.setTags(postRequestDto.getTags().toArray(new String[0]));
        } else {
            post.setTags(new String[0]);
        }

        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());

        Post savedPost = postService.savePost(post);

        return this.postMapper.mapPostToResponseDto(savedPost, user, Collections.emptyList(), false);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Удалить свой пост", description = "Удаляет пост по ID, если он принадлежит аутентифицированному пользователю")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiErrorResponse403
    public void deletePost(
            @RequestHeader(name = "Authorization") String token,
            @PathVariable @NotNull(message = "ID поста не может быть пустым.") @Min(value = 1, message = "ID должен быть больше 0.") Long postId
    ) {
        String authorization = token.substring(7);

        jwtHandler.validateToken(authorization);
        String authorLogin = jwtHandler.getUsernameFromToken(authorization);

        this.validateField(authorLogin, "Некорректное имя");

        User postAuthor = userService.getUserByLogin(authorLogin);

        if (Objects.isNull(postAuthor)) {
            throw new UserNotFoundException("Не удалось найти автора: " + authorLogin);
        }

        Post postToDelete = postService.getPostById(postId);

        if (!Objects.equals(postToDelete.getAuthor().getId(), postAuthor.getId())) {
            throw new SecurityException("Ошибка: вы не можете удалить чужую публикацию.");
        }

        postService.deletePost(postToDelete);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои посты", description = "Возвращает список постов, созданных аутентифицированным пользователем")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PostResponseDto.class)))
    )
    public List<PostResponseDto> myPosts(
            @RequestHeader(name = "Authorization") String token
    ) {
        String authorization = token.substring(7);

        jwtHandler.validateToken(authorization);
        String login = jwtHandler.getUsernameFromToken(authorization);

        this.validateField(login, "Некорректное имя");

        User postAuthor = userService.getUserByLogin(login);

        if (Objects.isNull(postAuthor)) {
            throw new UserNotFoundException("Не удалось найти автора: " + login);
        }

        List<Post> posts = postService.getPostsByUser(postAuthor);

        if (Objects.isNull(posts)) {
            return Collections.emptyList();
        }

        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .toList();

        // 3. Делаем ОДИН сетевой запрос за комментариями для всех постов сразу (Batch)
        Map<Long, List<CommentResponseDto>> commentsMap;
        try {
            commentsMap = commentsClient.getCommentsBatch(postIds, token);
        } catch (Exception e) {
            // Fallback: если сервис комментариев лежит, возвращаем пустую карту, чтобы лента работала
            commentsMap = Collections.emptyMap();
        }

        Map<Long, List<CommentResponseDto>> finalCommentsMap = commentsMap;

        return posts.stream()
                .map(post -> {
                    // Достаем комментарии для конкретного поста из Map
                    List<CommentResponseDto> postComments = finalCommentsMap.getOrDefault(post.getPostId(), Collections.emptyList());
                    // Передаем их в маппер
                    return postMapper.mapPostToResponseDto(post, post.getAuthor(), postComments, false);
                })
                .toList();
    }

    @GetMapping("/feed")
    @Operation(summary = "Персональная лента", description = "Посты пользователей, на которых я подписан")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PostResponseDto.class)))
    )
    public List<PostResponseDto> getFeed(
            @RequestHeader(name = "Authorization") String token
    ) {
        String authorization = token.substring(7);
        jwtHandler.validateToken(authorization);

        String login = jwtHandler.getUsernameFromToken(authorization);
        User currentUser = userService.getUserByLogin(login);

        String xUserId = jwtHandler.extractUserId(authorization);
        Long userId = Long.valueOf(xUserId);

        // Используем новый метод сервиса
        List<Post> feedPosts = postService.getFeedPosts(currentUser);

        if (feedPosts == null || feedPosts.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> postIds = feedPosts.stream()
                .map(Post::getPostId)
                .toList();

        // 3. Batch запрос за ЛАЙКАМИ (Новый код)
        Set<Long> likedPostIds = new HashSet<>();
        try {
            // Мы просто передаем токен дальше. Нам не нужно знать ID юзера здесь.
            List<Long> likedIds = likesClient.getLikedPostIds(postIds, userId, authorization);
            likedPostIds.addAll(likedIds);
        } catch (Exception e) {
            System.err.println("ОШИБКА при получении лайков: " + e.getMessage());
            e.printStackTrace();

            likedPostIds = Collections.emptySet();
        }

        Set<Long> finalLikedPostIds = likedPostIds;

        return feedPosts.stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getPostId());
                    return postMapper.mapPostToResponseDto(
                            post,
                            post.getAuthor(),
                            Collections.emptyList(),
                            isLiked
                    );
                })
                .toList();
    }

    @GetMapping("/all")
    @Operation(summary = "Получить все посты", description = "Возвращает полный список постов всех пользователей")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PostResponseDto.class)))
    )
    public List<PostResponseDto> allPosts(
            @RequestHeader(name = "Authorization") String token
    ) {
        String authorization = token.substring(7);

        jwtHandler.validateToken(authorization);

        String xUserId = jwtHandler.extractUserId(authorization);
        Long userId = Long.valueOf(xUserId);

        List<Post> allPosts = postService.getAllPosts();

        if (Objects.isNull(allPosts)) {
            return Collections.emptyList();
        }

        List<Long> postIds = allPosts.stream()
                .map(Post::getPostId)
                .toList();

        // 3. Делаем ОДИН сетевой запрос за комментариями для всех постов сразу (Batch)
        Map<Long, List<CommentResponseDto>> commentsMap;
        try {
            commentsMap = commentsClient.getCommentsBatch(postIds, token);
        } catch (Exception e) {
            // Fallback: если сервис комментариев лежит, возвращаем пустую карту, чтобы лента работала
            commentsMap = Collections.emptyMap();
        }

        // 3. Batch запрос за ЛАЙКАМИ (Новый код)
        Set<Long> likedPostIds = new HashSet<>();
        try {
            // Мы просто передаем токен дальше. Нам не нужно знать ID юзера здесь.
            List<Long> likedIds = likesClient.getLikedPostIds(postIds, userId, authorization);
            likedPostIds.addAll(likedIds);
        } catch (Exception e) {
            System.err.println("ОШИБКА при получении лайков: " + e.getMessage());
            e.printStackTrace();

             likedPostIds = Collections.emptySet();
        }

        Map<Long, List<CommentResponseDto>> finalCommentsMap = commentsMap;
        Set<Long> finalLikedPostIds = likedPostIds;

        return allPosts.stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getPostId());
                    return postMapper.mapPostToResponseDto(
                            post,
                            post.getAuthor(),
                            finalCommentsMap.getOrDefault(post.getPostId(), Collections.emptyList()),
                            isLiked
                    );
                })
                .toList();
    }

    @GetMapping("/by-tag")
    @Operation(summary = "Получить посты по тегу", description = "Находит и возвращает все посты, содержащие указанный тег")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PostResponseDto.class)))
    )
    public List<PostResponseDto> postsByTag(
            @RequestHeader(name = "Authorization") String token,
            @RequestParam(name = "tag") @NotBlank(message = "Тег не может быть пустым.") String tag
    ) {
        String authorization = token.substring(7);

        jwtHandler.validateToken(authorization);
        String xUserId = jwtHandler.extractUserId(authorization);
        Long userId = Long.valueOf(xUserId);

        List<Post> postsByTag = postService.getPostsByTag(tag);

        if (Objects.isNull(postsByTag)) {
            throw new PostNotFoundException("Не удалось найти публикации по тегу " + tag);
        }

        List<Long> postIds = postsByTag.stream()
                .map(Post::getPostId)
                .toList();

        // 3. Делаем ОДИН сетевой запрос за комментариями для всех постов сразу (Batch)
        Map<Long, List<CommentResponseDto>> commentsMap;
        try {
            commentsMap = commentsClient.getCommentsBatch(postIds, token);
        } catch (Exception e) {
            // Fallback: если сервис комментариев лежит, возвращаем пустую карту, чтобы лента работала
            commentsMap = Collections.emptyMap();
        }

        // 3. Batch запрос за ЛАЙКАМИ (Новый код)
        Set<Long> likedPostIds = new HashSet<>();
        try {
            // Мы просто передаем токен дальше. Нам не нужно знать ID юзера здесь.
            List<Long> likedIds = likesClient.getLikedPostIds(postIds, userId, authorization);
            likedPostIds.addAll(likedIds);
        } catch (Exception e) {
            System.err.println("ОШИБКА при получении лайков: " + e.getMessage());
            e.printStackTrace();

            likedPostIds = Collections.emptySet();
        }

        Map<Long, List<CommentResponseDto>> finalCommentsMap = commentsMap;
        Set<Long> finalLikedPostIds = likedPostIds;

        return postsByTag.stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getPostId());
                    return postMapper.mapPostToResponseDto(
                            post,
                            post.getAuthor(),
                            finalCommentsMap.getOrDefault(post.getPostId(), Collections.emptyList()),
                            isLiked
                    );
                })
                .toList();
    }

    @GetMapping("/by-login")
    @Operation(summary = "Получить посты по логину", description = "Возвращает список постов конкретного пользователя по его логину")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PostResponseDto.class)))
    )
    public List<PostResponseDto> postsByLogin(
            @RequestHeader(name = "Authorization") String token,
            @RequestParam(name = "login") @NotBlank(message = "Логин не может быть пустым.") String login
    ) {
        String authorization = token.substring(7);

        jwtHandler.validateToken(authorization);
        String xUserId = jwtHandler.extractUserId(authorization);
        Long userId = Long.valueOf(xUserId);

        User author = userService.getUserByLogin(login);

        if (Objects.isNull(author)) {
            throw new UserNotFoundException("Не удалось найти пользователя по логину: " + login);
        }

        List<Post> postsByLogin = postService.getPostsByUser(author);

        if (postsByLogin == null || postsByLogin.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> postIds = postsByLogin.stream()
                .map(Post::getPostId)
                .toList();

        // 3. Делаем ОДИН сетевой запрос за комментариями для всех постов сразу (Batch)
        Map<Long, List<CommentResponseDto>> commentsMap;
        try {
            commentsMap = commentsClient.getCommentsBatch(postIds, token);
        } catch (Exception e) {
            // Fallback: если сервис комментариев лежит, возвращаем пустую карту, чтобы лента работала
            commentsMap = Collections.emptyMap();
        }

        // 3. Batch запрос за ЛАЙКАМИ (Новый код)
        Set<Long> likedPostIds = new HashSet<>();
        try {
            // Мы просто передаем токен дальше. Нам не нужно знать ID юзера здесь.
            List<Long> likedIds = likesClient.getLikedPostIds(postIds, userId, authorization);
            likedPostIds.addAll(likedIds);
        } catch (Exception e) {
            System.err.println("ОШИБКА при получении лайков: " + e.getMessage());
            e.printStackTrace();

            likedPostIds = Collections.emptySet();
        }

        Map<Long, List<CommentResponseDto>> finalCommentsMap = commentsMap;
        Set<Long> finalLikedPostIds = likedPostIds;

        return postsByLogin.stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getPostId());
                    return postMapper.mapPostToResponseDto(
                            post,
                            post.getAuthor(),
                            finalCommentsMap.getOrDefault(post.getPostId(), Collections.emptyList()),
                            isLiked
                    );
                })
                .toList();
    }

    @GetMapping("/by-postId/{postId}")
    @Operation(summary = "Получить пост по ID", description = "Возвращает полную информацию о посте по его идентификатору")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PostResponseDto.class))
    )
    public PostResponseDto getPostById(
            @RequestHeader(name = "Authorization") String token,
            @PathVariable @NotNull(message = "Id поста не может быть пустым.") @Min(value = 1, message = "Id поста не может быть меньше 1.") Long postId
    ) {
        String authorization = token.substring(7);

        jwtHandler.validateToken(authorization);
        String xUserId = jwtHandler.extractUserId(authorization);
        Long userId = Long.valueOf(xUserId);

        Post postById = postService.getPostById(postId);

        if (Objects.isNull(postById)) {
            throw new PostNotFoundException("Не удалось найти публикацию по id: " + postId);
        }

        List<CommentResponseDto> commentsList;
        try {
            commentsList = commentsClient.getCommentsByPostId(postId, token);
        } catch (Exception e) {
            commentsList = Collections.emptyList();
        }

        boolean isLiked = false;

        // Используем тот же метод, просто передаем список из одного элемента
        List<Long> likedIds = likesClient.getLikedPostIds(List.of(postId), userId, authorization);
        if (!likedIds.isEmpty()) {
            isLiked = true;
        }

        List<CommentResponseDto> finalCommentsList = commentsList;

        return postMapper.mapPostToResponseDto(postById, postById.getAuthor(), finalCommentsList, isLiked);
    }

    @GetMapping("/by-user-type")
    @Operation(summary = "Получить посты по типу пользователя", description = "Возвращает все посты, опубликованные пользователями указанного типа (PERSON или ORGANIZATION)")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PostResponseDto.class)))
    )
    public List<PostResponseDto> postsByUserType(
            @RequestHeader(name = "Authorization") String token,
            @RequestParam(name = "userType") @NotNull(message = "Некорректный тип пользователя.") UserType userType
    ) {
        String authorization = token.substring(7);

        jwtHandler.validateToken(authorization);
        String xUserId = jwtHandler.extractUserId(authorization);
        Long userId = Long.valueOf(xUserId);

        List<Post> postsByUserType = postService.getPostsByUserType(userType);

        if (Objects.isNull(postsByUserType)) {
            throw new PostNotFoundException("Не удалось найти публикации по типу пользователя: " + userType);
        }

        List<Long> postIds = postsByUserType.stream()
                .map(Post::getPostId)
                .toList();

        // 3. Делаем ОДИН сетевой запрос за комментариями для всех постов сразу (Batch)
        Map<Long, List<CommentResponseDto>> commentsMap;
        try {
            commentsMap = commentsClient.getCommentsBatch(postIds, token);
        } catch (Exception e) {
            // Fallback: если сервис комментариев лежит, возвращаем пустую карту, чтобы лента работала
            commentsMap = Collections.emptyMap();
        }

        Set<Long> likedPostIds = new HashSet<>();
        try {
            // Мы просто передаем токен дальше. Нам не нужно знать ID юзера здесь.
            List<Long> likedIds = likesClient.getLikedPostIds(postIds, userId, authorization);
            likedPostIds.addAll(likedIds);
        } catch (Exception e) {
            likedPostIds = Collections.emptySet();
        }

        Map<Long, List<CommentResponseDto>> finalCommentsMap = commentsMap;
        Set<Long> finalLikedPostIds = likedPostIds;

        return postsByUserType.stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getPostId());
                    return postMapper.mapPostToResponseDto(
                            post,
                            post.getAuthor(),
                            finalCommentsMap.getOrDefault(post.getPostId(), Collections.emptyList()),
                            isLiked
                    );
                })
                .toList();
    }

    private void validateField(String field, String errorMessage) {
        if (Objects.isNull(field) || field.trim().isEmpty()) {
            throw new AgoraIllegalArgumentException(errorMessage);
        }
    }

}
