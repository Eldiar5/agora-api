package agora.service.impl;

import agora.service.metrics.BusinessMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.exceptions.UserNotFoundException;
import agora.repository.UserRepository;
import agora.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final BusinessMetricsService metricsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findUserByLogin(username);

        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return user;
    }

    @Transactional
    @Override
    public User saveNewUser(User user, String clearPassword) throws UserNotFoundException {
        user.setPasswordHash(passwordEncoder.encode(clearPassword));
        metricsService.incrementUsers();
        return this.userRepository.save(user);
    }

    @Transactional
    @Override
    public User saveUser(User user) {
        return this.userRepository.save(user);
    }

    @Override
    public boolean isUserExists(String login) {
        try {
            User user = this.getUserByLogin(login);
            return Objects.nonNull(user);
        } catch (UserNotFoundException ex) {
            return false;
        }
    }

    @Override
    public User getUserByLogin(String login) throws UserNotFoundException {
        return this.userRepository.findUserByLogin(login);
    }

    @Override
    public User getUserById(Long id) throws UserNotFoundException {
        return this.userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id + ";"));
    }

    @Override
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @Override
    public List<User> getAllUsersByUserType(UserType userType) throws UserNotFoundException {
        return this.userRepository.findAllByUserType(userType);
    }

    @Override
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.searchByUsername(query.trim());
    }
}
