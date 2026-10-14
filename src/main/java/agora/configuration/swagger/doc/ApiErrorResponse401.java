package agora.configuration.swagger.doc;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import agora.dto.response.ErrorResponseDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // Позволяет вешать на класс
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "401",
        description = "Не авторизован (Unauthorized). JWT токен отсутствует, просрочен или недействителен.",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
        )
)
public @interface ApiErrorResponse401 {
}
