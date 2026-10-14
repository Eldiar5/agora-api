package agora.service;

import agora.dto.response.TokenResponse;

public interface AuthService {

    TokenResponse generateTokenPair(String username, Long userId, String avatarUrl);

    TokenResponse refreshTokenPair(String refreshToken);

}
