package agora.service.impl;

import agora.configuration.MinioConfig;
import agora.controller.ImageController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import agora.configuration.RabbitMqConfig;
import agora.entity.user.Person;
import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.exceptions.UserNotFoundException;
import agora.repository.UserRepository;
import agora.service.UserService;

import java.time.LocalDate;
import java.util.Objects;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private RabbitMqConfig rabbitMqConfig;
    @MockitoBean
    private MinioConfig minioConfig;
    @MockitoBean
    private ImageServiceImpl imageService;
    @MockitoBean
    private ImageController imageController;

    @Test
    public void saveNewUser() throws UserNotFoundException {
        Person person = new Person();

        person.setUserType(UserType.PERSON);

        person.setLogin("eldik");
        person.setPasswordHash("12345");

        person.setName("Eldiar");
        person.setLastName("Mambetaliev");
        person.setBirthDate(LocalDate.parse("2001-01-01"));

        userService.saveNewUser(person, person.getPassword());

        User user = userRepository.findUserByLogin(person.getLogin());

        if (Objects.isNull(user)) {
            throw new UserNotFoundException("Пользователь с логином " + person.getLogin() + "не найден.");
        }

        assert(user.getLogin().equals(person.getLogin()));
    }

}
