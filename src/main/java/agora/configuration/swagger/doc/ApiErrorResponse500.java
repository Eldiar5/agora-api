package agora.configuration.swagger.doc;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import agora.dto.response.ErrorResponseDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // Важно: TYPE позволяет вешать на класс
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "500",
        description = "Внутренняя ошибка сервера (Internal Server Error)",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
        )
)
public @interface ApiErrorResponse500 {
}
