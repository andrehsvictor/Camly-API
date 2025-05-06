package andrehsvictor.camly.account;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import andrehsvictor.camly.account.dto.AccountDto;
import andrehsvictor.camly.account.dto.CreateAccountDto;
import andrehsvictor.camly.account.dto.UpdateAccountDto;
import andrehsvictor.camly.user.User;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDto userToAccountDto(User user);

    User createAccountDtoToUser(CreateAccountDto createAccountDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User updateUserFromUpdateAccountDto(
            UpdateAccountDto updateAccountDto,
            @MappingTarget User user);

    @AfterMapping
    default void afterMapping(UpdateAccountDto updateAccountDto, @MappingTarget User user) {
        if (updateAccountDto.getPictureUrl() != null && !updateAccountDto.getPictureUrl().isBlank()) {
            user.setPictureUrl(null);
        }
        if (updateAccountDto.getBio() != null && !updateAccountDto.getBio().isBlank()) {
            user.setBio(null);
        }
    }

}
