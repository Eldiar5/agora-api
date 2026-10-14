package agora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import agora.configuration.swagger.doc.ApiErrorResponse400;
import agora.configuration.swagger.doc.ApiErrorResponse500;
import agora.dto.request.RegistrationRequestDto;
import agora.dto.response.RegistrationResponseDto;
import agora.entity.user.Organization;
import agora.entity.user.Person;
import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.exceptions.AgoraIllegalArgumentException;
import agora.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/register")
@Tag(name = "Registration API", description = "API для регистрации новых пользователей (Person или Organization)")
@ApiErrorResponse500
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя (Person или Organization) на основе предоставленных данных")
    @ApiErrorResponse400
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponseDto register(
            @RequestBody @Valid RegistrationRequestDto requestDto
    ) {
        if (userService.isUserExists(requestDto.getLogin())) {
            throw new AgoraIllegalArgumentException("Пользователь под таким логином уже есть.");
        }

        UserType userType = UserType.getUserType(requestDto.getUserType());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (UserType.PERSON.equals(userType)) {
            Person user = new Person();

            validateField(requestDto.getName(), "Имя не может быть пустым.");
            validateField(requestDto.getLastName(), "Фамилия не может быть пустой.");
            LocalDate birthDate = validateDate(requestDto.getBirthDate(), "рождения");

            if (birthDate.isBefore(LocalDate.now().minusYears(100))) {
                throw new AgoraIllegalArgumentException("Дата рождения не может превышать 100 лет.");
            }

            user.setUserType(userType);
            user.setLogin(requestDto.getLogin());
            user.setName(requestDto.getName());
            user.setLastName(requestDto.getLastName());
            user.setBirthDate(birthDate);

            User savedUser = userService.saveNewUser(user, requestDto.getPassword());

            RegistrationResponseDto responseDto = new RegistrationResponseDto();
            responseDto.setId(savedUser.getId());
            responseDto.setLogin(savedUser.getLogin());
            responseDto.setName(user.getName());
            responseDto.setLastName(user.getLastName());
            responseDto.setBirthDate(user.getBirthDate().format(formatter));

            return responseDto;

        } else {
            Organization user = new Organization();

            validateField(requestDto.getTitle(), "Название организации не может быть пустым");
            validateField(requestDto.getSpecialization(), "Род деятельности не может быть пустым");
            LocalDate dateOfFoundation = validateDate(requestDto.getDateOfFoundation(), "основания");

            if (dateOfFoundation.isBefore(LocalDate.now().minusYears(225))) {
                throw new AgoraIllegalArgumentException("Дата основания не может превышать 1800 год.");
            }

            user.setUserType(userType);
            user.setLogin(requestDto.getLogin());
            user.setTitle(requestDto.getTitle());
            user.setSpecialization(requestDto.getSpecialization());
            user.setDateOfFoundation(dateOfFoundation);

            User savedUser = userService.saveNewUser(user, requestDto.getPassword());

            RegistrationResponseDto responseDto = new RegistrationResponseDto();
            responseDto.setLogin(savedUser.getLogin());
            responseDto.setId(savedUser.getId());
            responseDto.setTitle(user.getTitle());
            responseDto.setSpecialization(user.getSpecialization());
            responseDto.setDateOfFoundation(user.getDateOfFoundation().format(formatter));

            return responseDto;
        }

    }

    private void validateField(String field, String errorMessage) {
        if (Objects.isNull(field) || field.trim().isEmpty()) {
            throw new AgoraIllegalArgumentException(errorMessage);
        }
    }

    private LocalDate validateDate(String date, String dateType) {
        if (Objects.isNull(date) || date.trim().isEmpty()) {
            throw new AgoraIllegalArgumentException("Дата " + dateType + " не может быть пустой");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedDate = LocalDate.parse(date, formatter);
            if (parsedDate.isAfter(LocalDate.now())) {
                throw new AgoraIllegalArgumentException("Дата " + dateType + " не может быть в будущем");
            }
            return parsedDate;
        } catch (DateTimeParseException e) {
            throw new AgoraIllegalArgumentException("Неверная дата " + dateType + ". Проверьте корректность числа месяца и дня.");
        }
    }

}
