package andrehsvictor.camly.jwt;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
public class JwtLifespanProperties {

    @Value("${camly.jwt.access-token.lifespan:15m}")
    private Duration accessTokenLifespan;

    @Value("${camly.jwt.refresh-token.lifespan:1h}")
    private Duration refreshTokenLifespan;

}
