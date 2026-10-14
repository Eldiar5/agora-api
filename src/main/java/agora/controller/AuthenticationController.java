package agora.controller;

import agora.dto.response.TokenResponse;
import agora.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import agora.configuration.swagger.doc.ApiErrorResponse400;
import agora.configuration.swagger.doc.ApiErrorResponse404;
import agora.configuration.swagger.doc.ApiErrorResponse500;
import agora.dto.request.LoginRequestDto;
import agora.dto.response.InfoResponseDto;
import agora.dto.response.LoginResponseDto;
import agora.entity.user.User;
import agora.exceptions.AgoraIllegalArgumentException;
import agora.security.JwtHandler;
import agora.service.UserService;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "API для входа в систему и получения JWT токена")
@ApiErrorResponse500
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя", description = "Аутентифицирует пользователя по логину и паролю и возвращает JWT токен")
    @ApiErrorResponse400
    @ApiErrorResponse404
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InfoResponseDto.class))
    )
    public LoginResponseDto login(@RequestBody @Valid LoginRequestDto request) {

        User user = userService.getUserByLogin(request.getLogin());

        if (user == null) {
            throw new AgoraIllegalArgumentException("Не удалось найти пользователя с таким логином.");
        }

        String hashedPassword = user.getPasswordHash();

        if (Objects.isNull(hashedPassword) || hashedPassword.isEmpty()) {
            throw new AgoraIllegalArgumentException("Ошибка безопасности: у пользователя отсутствует хеш пароля.");
        }

        if (!passwordEncoder.matches(request.getPassword(), hashedPassword)) {
            throw new AgoraIllegalArgumentException("Введен неверный пароль.");
        }

        TokenResponse token = this.authService.generateTokenPair(user.getLogin(), user.getId(), user.getAvatarUrl());

        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(token);
        loginResponseDto.setLogin(user.getLogin());

        return loginResponseDto;
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshTokens(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponse response = authService.refreshTokenPair(refreshToken);
        return ResponseEntity.ok(response);
    }
}