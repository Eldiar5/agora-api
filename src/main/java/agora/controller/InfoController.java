package agora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import agora.configuration.swagger.doc.ApiErrorResponse400;
import agora.configuration.swagger.doc.ApiErrorResponse401;
import agora.configuration.swagger.doc.ApiErrorResponse404;
import agora.configuration.swagger.doc.ApiErrorResponse500;
import agora.dto.response.InfoResponseDto;
import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.exceptions.AgoraIllegalArgumentException;
import agora.exceptions.UserNotFoundException;
import agora.mapper.v2.HttpUserMapper;
import agora.service.UserService;

import java.util.List;
import java.util.Objects;

@Validated
@RestController
@RequestMapping("/api/v1/info")
@Transactional(readOnly = true)
@Tag(name = "UserInfo API", description = "API для получения информации о себе и других пользователях (Требует JWT токен)")
@ApiErrorResponse500
@ApiErrorResponse401
@ApiErrorResponse404
@ApiErrorResponse400
public class InfoController {

    private final UserService userService;
    private final HttpUserMapper userMapper;

    @Autowired
    public InfoController(UserService userService, HttpUserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/my")
    @Operation(summary = "Получить информацию о себе", description = "Возвращает информацию о пользователе, которому принадлежит JWT токен")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InfoResponseDto.class))
    )
    public InfoResponseDto info(
            @AuthenticationPrincipal User user
    ) {
        return userMapper.mapUserToResponseDto(user);
    }

    @GetMapping("/by-login")
    @Operation(summary = "Получить информацию по логину", description = "Возвращает публичную информацию о пользователе по его логину")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InfoResponseDto.class))
    )
    public InfoResponseDto infoByLogin (
            @RequestParam(name = "login") @NotBlank(message = "Логин не может быть пустым") String userLogin
    ) {
        User user = userService.getUserByLogin(userLogin);

        if (user == null) {
            throw new UserNotFoundException("Не удалось найти пользователя с логином: " + userLogin);
        }

        return userMapper.mapUserToResponseDto(user);
    }

    @GetMapping("/by-userId/{userId}")
    @Operation(summary = "Получить пользователя по ID")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InfoResponseDto.class))
    )
    public InfoResponseDto getUserById(
            @PathVariable @NotNull(message = "ID не может быть null") @Min(value = 1, message = "ID должен быть больше 0") Long userId
    ) {
        User user = userService.getUserById(userId);

        if (Objects.isNull(user)) {
            throw new UserNotFoundException("Не удалось найти пользователя с id: " + userId);
        }

        return userMapper.mapUserToResponseDto(user);
    }

    @GetMapping("/all/by-user-type")
    @Operation(summary = "Получить пользователей по типу", description = "Возвращает список пользователей, отфильтрованных по типу (PERSON или ORGANIZATION)")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = InfoResponseDto.class)))
    )
    public List<InfoResponseDto> infoAllByUserType (
            @RequestParam(name = "userType") @NotNull(message = "Тип пользователя не может быть null") UserType userType
    ) {
        return this.userService.getAllUsersByUserType(userType)
                .stream()
                .map(userMapper::mapUserToResponseDto)
                .toList();
    }

    @GetMapping("/all")
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех зарегистрированных пользователей")
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = InfoResponseDto.class)))
    )
    public List<InfoResponseDto> infoAll () {
        if (userService.getAllUsers().isEmpty()) {
            throw new AgoraIllegalArgumentException("Список пользователей пуст.");
        }

        return this.userService.getAllUsers()
                .stream()
                .map(userMapper::mapUserToResponseDto)
                .toList();
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск пользователей", description = "Ищет пользователей по частичному совпадению логина")
    public List<InfoResponseDto> searchUsers(
            @RequestParam @NotBlank(message = "Поисковый запрос не может быть пустым") String query
    ) {
        List<User> users = userService.searchUsers(query);

        // (Мы не заполняем тут подписчиков, чтобы поиск работал быстро)
        return users.stream()
                .map(userMapper::mapUserToShortDto)
                .toList();
    }
}
