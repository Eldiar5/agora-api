package agora.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationResponseDto {

    private Long id;
    private String login;

    private String name;
    private String lastName;
    private String birthDate;

    private String dateOfFoundation;
    private String title;
    private String specialization;

}
