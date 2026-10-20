package agora.service.impl;

import agora.service.metrics.BusinessMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import agora.dto.event.PostCreatedEvent;
import agora.entity.post.Post;
import agora.entity.post.PostJpaEntity;
import agora.entity.tags.Tags;
import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.exceptions.PostNotFoundException;
import agora.exceptions.UserNotFoundException;
import agora.mapper.v1.PostJpaMapper;
import agora.repository.PostRepository;
import agora.repository.SubscriptionRepository;
import agora.repository.TagRepository;
import agora.service.PostService;
import agora.service.UserService;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private static final String POSTS_TOPIC = "agora.posts";

    private final UserService userService;
    private final PostRepository postRepository;
    private final PostJpaMapper postMapper;
    private final TagRepository tagRepository;
    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final BusinessMetricsService metricsService;

    @Override
    @Transactional
    public Post savePost(Post post) throws PostNotFoundException {
        PostJpaEntity entityToSave = this.postMapper.toEntity(post);

        Set<String> tagNames = new HashSet<>(List.of(post.getTags()));

        // 2. Одним запросом достаем все теги, которые УЖЕ есть в базе
        List<Tags> existingTags = tagRepository.findByTagNameIn(tagNames);

        Map<String, Tags> existingTagsMap = existingTags.stream()
                .collect(Collectors.toMap(Tags::getTagName, tag -> tag));

        // 4. Формируем итоговый список тегов для поста
        List<Tags> resultTags = new ArrayList<>();

        for (String tagName : tagNames) {
            String trimmedName = tagName.trim();
            if (trimmedName.isEmpty()) continue;

            // Если тег есть в мапе — берем его, если нет — создаем новый
            if (existingTagsMap.containsKey(trimmedName)) {
                resultTags.add(existingTagsMap.get(trimmedName));
            } else {
                Tags newTag = new Tags();
                newTag.setTagName(trimmedName);
                // Тут можно собрать новые теги в отдельный список и сохранить их через saveAll в конце,
                // но даже одиночные save() для НОВЫХ тегов лучше, чем save() для всех подряд.
                // Для идеала: собрать newTagsList -> saveAll(newTagsList) -> добавить в resultTags.
                resultTags.add(tagRepository.save(newTag));
            }
        }

        entityToSave.setTags(resultTags);
        PostJpaEntity savedPost = this.postRepository.save(entityToSave);

        metricsService.incrementPosts();

        PostCreatedEvent event = new PostCreatedEvent(
                savedPost.getPostId(),
                savedPost.getAuthor().getUsername(),
                LocalDateTime.now()
        );

        // Регистрируем действие, которое выполнится ТОЛЬКО после commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishPostCreated(event);
            }
        });

        return postMapper.toDomain(savedPost);
    }

    // Вызывается после commit: пост уже сохранён, поэтому сбой Kafka не должен ронять HTTP-ответ.
    // Событие при сбое теряется — надёжная доставка потребует Transactional Outbox.
    private void publishPostCreated(PostCreatedEvent event) {
        String key = event.getPostId().toString(); // ключ = postId → все события поста в одной партиции

        try {
            kafkaTemplate.send(POSTS_TOPIC, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            // Асинхронный сбой: брокер недоступен, истёк delivery.timeout.ms и т.п.
                            log.error("❌ [KAFKA] Не удалось отправить PostCreatedEvent (postId={}) в {}",
                                    event.getPostId(), POSTS_TOPIC, ex);
                            return;
                        }
                        var metadata = result.getRecordMetadata();
                        log.info("📤 [KAFKA] PostCreatedEvent отправлен: postId={}, partition={}, offset={}",
                                event.getPostId(), metadata.partition(), metadata.offset());
                    });
        } catch (Exception ex) {
            // Синхронный сбой ещё до отправки: ошибка сериализации, таймаут получения метаданных топика
            log.error("❌ [KAFKA] Ошибка при подготовке PostCreatedEvent (postId={})", event.getPostId(), ex);
        }
    }

    @Override
    public List<Post> getPostsByUser(User user) throws PostNotFoundException {
        List<PostJpaEntity> userPostsEntities = this.postRepository.findByAuthorOrderByCreatedAtDesc(user);

        return userPostsEntities.stream()
                .map(this.postMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getFeedPosts(User user) {
        // 1. Получаем список тех, на кого подписан юзер
        List<User> following = subscriptionRepository.findFollowingByFollower(user);

        if (following.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Запрашиваем посты этих авторов
        List<PostJpaEntity> followingPosts = postRepository.findByAuthorInOrderByCreatedAtDesc(following);

        return followingPosts.stream()
                .map(this.postMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getAllPosts() throws PostNotFoundException {

        List<PostJpaEntity> allPostsEntities = this.postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        if (allPostsEntities.isEmpty()) {
            throw new PostNotFoundException();
        }

        return allPostsEntities.stream()
                .map(postMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getPostsByTag(String tag) throws PostNotFoundException {

        List<PostJpaEntity> postsByTag = this.postRepository.findByTagName(tag);

        if (postsByTag.isEmpty()) {
            throw new PostNotFoundException("Не удалось найти публикации по тегу: #" + tag);
        }

        return postsByTag.stream()
                .map(postMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getPostsByUserType(UserType userType) throws PostNotFoundException, UserNotFoundException {
        List<User> usersByType = userService.getAllUsersByUserType(userType);

        if (usersByType.isEmpty()) {
            throw new UserNotFoundException();
        }

        List<PostJpaEntity> entitiesByUserType = this.postRepository.findByAuthorInOrderByCreatedAtDesc(usersByType);
        return entitiesByUserType.stream()
                .map(postMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Post getPostById(Long postId) throws PostNotFoundException {
        return this.postRepository.findById(postId).stream()
                .map(postMapper::toDomain)
                .findAny().orElseThrow(() -> new PostNotFoundException("Пост с ID: " + postId + "не найден"));
    }

    @Override
    @Transactional
    public void deletePost(Post post) throws PostNotFoundException {
        if (Objects.isNull(post) || Objects.isNull(post.getPostId())) {
            throw new PostNotFoundException("Невозможно удалить пост с null ID");
        }

        if (this.postRepository.existsById(post.getPostId())) {
            this.postRepository.deleteById(post.getPostId());
            metricsService.decrementPosts();
        } else {
            throw new PostNotFoundException("Попытка удалить несуществующий пост с ID: " + post.getPostId());
        }
    }
}
