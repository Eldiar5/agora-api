package agora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class PostRequestDto {

    @NotBlank(message = "Тема публикации не может быть пустой.")
    private String topic;

    @NotBlank(message = "Текст публикации не может быть пустой.")
    private String text;

    private List<String> tags;

    private String imageUrl;

}
