package andrehsvictor.camly.user;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.camly.user.dto.UserDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/v1/users")
    public Page<UserDto> getAll(
        @RequestParam(required = false, name = "q") String query,
        @RequestParam(required = false, name = "username") String username,
        Pageable pageable
    ) {
        return userService.getAllWithFilters(query, username, pageable)
            .map(userService::toDto);
    }

    @GetMapping("/api/v1/users/{id}")
    public UserDto getById(@PathVariable UUID id) {
        return userService.toDto(userService.getById(id));
    }

}
