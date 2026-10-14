package agora.mapper.v2;

import agora.dto.response.InfoResponseDto;
import agora.entity.user.User;

public interface HttpUserMapper {

    InfoResponseDto mapUserToResponseDto(User user);

    InfoResponseDto mapUserToShortDto(User user);

}
