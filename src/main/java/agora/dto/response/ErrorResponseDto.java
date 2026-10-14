package agora.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Стандартный ответ об ошибке")
public class ErrorResponseDto {

    @Schema(description = "Текст ошибки, который увидит пользователь", example = "Some response")
    private String error;

}
