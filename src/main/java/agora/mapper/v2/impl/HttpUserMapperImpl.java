package agora.mapper.v2.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import agora.dto.response.InfoResponseDto;
import agora.entity.user.Organization;
import agora.entity.user.Person;
import agora.entity.user.User;
import agora.entity.user.UserType;
import agora.mapper.v2.HttpUserMapper;
import agora.service.impl.SubscriptionServiceImpl;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class HttpUserMapperImpl implements HttpUserMapper {

    private final SubscriptionServiceImpl subscriptionService;

    @Override
    public InfoResponseDto mapUserToResponseDto(User user) {
        InfoResponseDto responseDto = this.mapCommonFields(user);

        responseDto.setFollowersCount(subscriptionService.getFollowersCount(user));
        responseDto.setFollowingCount(subscriptionService.getFollowingCount(user));

        return responseDto;
    }

    @Override
    public InfoResponseDto mapUserToShortDto(User user) {
        return this.mapCommonFields(user);
    }

    private InfoResponseDto mapCommonFields(User user) {
        InfoResponseDto responseDto = new InfoResponseDto();
        responseDto.setId(user.getId());
        responseDto.setLogin(user.getLogin());
        responseDto.setAvatarUrl(user.getAvatarUrl());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        if (UserType.PERSON.equals(user.getUserType())) {
            Person person = (Person) user;
            responseDto.setName(person.getName());
            responseDto.setLastName(person.getLastName());
            if (person.getBirthDate() != null) {
                responseDto.setBirthDate(person.getBirthDate().format(formatter));
            }
        } else if (UserType.ORGANIZATION.equals(user.getUserType())) {
            Organization organization = (Organization) user;
            responseDto.setTitle(organization.getTitle());
            responseDto.setSpecialization(organization.getSpecialization());
            if (organization.getDateOfFoundation() != null) {
                responseDto.setDateOfFoundation(organization.getDateOfFoundation().format(formatter));
            }
        }

        return responseDto;
    }

}
