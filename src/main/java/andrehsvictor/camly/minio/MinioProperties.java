package andrehsvictor.camly.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "camly.minio")
public class MinioProperties {

    private String adminUsername;
    private String adminPassword;
    private String endpoint;
    private String bucketName;

}
