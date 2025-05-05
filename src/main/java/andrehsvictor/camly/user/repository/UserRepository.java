package andrehsvictor.camly.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andrehsvictor.camly.user.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}
