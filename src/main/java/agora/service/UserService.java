package agora.service;

import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.exceptions.UserNotFoundException;

import java.util.List;

public interface UserService {

    User saveNewUser(User user, String rawPassword) throws UserNotFoundException;

    boolean isUserExists(String login) throws UserNotFoundException;

    User getUserByLogin(String login) throws UserNotFoundException;

    User getUserById(Long id) throws UserNotFoundException;

    List<User> getAllUsers() throws UserNotFoundException;

    List<User> getAllUsersByUserType(UserType userType) throws UserNotFoundException;

    List<User> searchUsers(String query);

    // Простой метод для сохранения обновлений (без смены пароля)
    User saveUser(User user);

}
