package andrehsvictor.camly.minio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import andrehsvictor.camly.util.ClasspathFileService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    @Value("${camly.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${camly.minio.admin.username:minioadmin}")
    private String username;

    @Value("${camly.minio.admin.password:minioadmin}")
    private String password;

    @Value("${camly.minio.bucket.name:camly}")
    private String bucketName;

    private final ClasspathFileService classpathFileService;

    @Bean
    MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username, password)
                .build();

        setupBucket(minioClient);
        setupBucketPolicy(minioClient);

        return minioClient;
    }

    private void setupBucket(MinioClient minioClient) {
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();

            if (!minioClient.bucketExists(bucketExistsArgs)) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build();
                minioClient.makeBucket(makeBucketArgs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bucket", e);
        }
    }

    private void setupBucketPolicy(MinioClient minioClient) {
        try {
            String bucketPolicy = classpathFileService.getContent("bucket-policy.json");
            SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(bucketPolicy)
                    .build();
            minioClient.setBucketPolicy(setBucketPolicyArgs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set bucket policy", e);
        }
    }
}