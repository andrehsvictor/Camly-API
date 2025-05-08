package andrehsvictor.camly.user;

import org.mapstruct.Mapper;

import andrehsvictor.camly.user.dto.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto userToUserDto(User user);

}
