package agora.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RegistrationRequestDto {

    @NotNull(message = "Тип пользователя не может быть пустым.")
    @Min(value = 0, message = "Введен некорректный тип пользователя. Допустимо 0 или 1")
    @Max(value = 1, message = "Введен некорректный тип пользователя. Допустимо 0 или 1")
    private Integer userType;

    @NotBlank(message = "Логин не может быть пустым.")
    @Size(min = 3, message = "Логин не может быть менее 3 символов.")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым.")
    @Size(min = 5, message = "Пароль не может быть менее 5 символов.")
    @Pattern(regexp = "^\\S+$", message = "Пароль не может содержать пробелы.")
    private String password;

    private String name;
    private String lastName;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Неверный формат даты. Ожидается yyyy-MM-dd")
    private String birthDate;

    private String title;
    private String specialization;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Неверный формат даты. Ожидается yyyy-MM-dd")
    private String dateOfFoundation;

}
