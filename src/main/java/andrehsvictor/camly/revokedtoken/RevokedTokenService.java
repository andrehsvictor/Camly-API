package andrehsvictor.camly.revokedtoken;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevokedTokenService {

    private final RedisTemplate<String, Integer> redisTemplate;

    private static final String PREFIX = "revoked_token:";

    public void revoke(Jwt jwt) {
        String tokenId = jwt.getId();
        Duration ttl = Duration.between(Instant.now(), jwt.getExpiresAt());
        redisTemplate.opsForValue().set(PREFIX + tokenId, 0, ttl);
    }

    public boolean isRevoked(Jwt jwt) {
        String tokenId = jwt.getId();
        return redisTemplate.hasKey(PREFIX + tokenId);
    }

}
