package agora.controller;

import agora.dto.response.InfoResponseDto;
import agora.entity.user.User;
import agora.mapper.v2.HttpUserMapper;
import agora.security.JwtHandler;
import agora.service.ImageService;
import agora.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "Управление профилем пользователя")
public class UserController {

    private final UserService userService;
    private final ImageService imageService;
    private final JwtHandler jwtHandler;
    private final HttpUserMapper userMapper;

    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Обновить аватар", description = "Загружает фото в MinIO и обновляет профиль")
    public InfoResponseDto updateAvatar(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file
    ) {
        // 1. Определяем, кто стучится (по токену)
        String login = jwtHandler.getUsernameFromToken(token.substring(7));
        User user = userService.getUserByLogin(login);

        // 2. Грузим картинку через наш сервис
        String avatarUrl = imageService.uploadImage(file);

        // 3. Обновляем юзера
        user.setAvatarUrl(avatarUrl);
        userService.saveUser(user); // Убедись, что такой метод есть (или userRepository.save)

        // 4. Возвращаем обновленное DTO
        return userMapper.mapUserToResponseDto(user);
    }

}
