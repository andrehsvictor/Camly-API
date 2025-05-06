package andrehsvictor.camly.account;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class ActionTokenLifespanProperties {

    @Value("${camly.action-token.email-verification.lifespan:6h}")
    private Duration emailVerificationTokenLifespan = Duration.ofHours(6);

    @Value("${camly.action-token.password-reset.lifespan:1h}")
    private Duration passwordResetTokenLifespan = Duration.ofHours(1);

}
