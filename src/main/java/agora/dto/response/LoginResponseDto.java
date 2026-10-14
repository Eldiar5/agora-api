package agora.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponseDto {
    TokenResponse token;
    private String login;
}
