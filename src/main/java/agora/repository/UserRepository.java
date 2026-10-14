package agora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import agora.entity.user.User;
import agora.entity.user.UserType;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByLogin(String login);

    List<User> findAllByUserType(UserType userType);

    // Ищем по username (без учета регистра)
    @Query("SELECT u FROM User u WHERE LOWER(u.login) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByUsername(@Param("query") String query);

}
