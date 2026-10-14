package agora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import agora.entity.tags.Tags;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tags, Long> {

    Optional<Tags> findByTagName(String tagName);

    List<Tags> findByTagNameIn(Set<String> tagNames);
}
