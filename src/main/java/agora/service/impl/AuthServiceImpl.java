package agora.service.impl;

import agora.dto.response.TokenResponse;
import agora.entity.user.User;
import agora.security.JwtHandler;
import agora.service.AuthService;
import agora.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtHandler jwtHandler;
    private final UserService userService;

    @Override
    public TokenResponse generateTokenPair(String username, Long userId, String avatarUrl) {
        String accessToken = jwtHandler.generateAccessToken(username, userId, avatarUrl);
        String refreshToken = jwtHandler.generateRefreshToken(username, userId);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    public TokenResponse refreshTokenPair(String refreshToken) {
        Claims claims = this.jwtHandler.getValidClaims(refreshToken, "refresh");

        String username = claims.getSubject();
        Long userId = claims.get("id", Long.class);

        User user = userService.getUserById(userId);
        String avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : null;

        return generateTokenPair(username, userId, avatarUrl);
    }
}