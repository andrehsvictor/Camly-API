package andrehsvictor.camly.minio;

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

    private final MinioProperties minioProperties;
    private final ClasspathFileService classpathFileService;

    @Bean
    MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAdmin().get("username"),
                        minioProperties.getAdmin().get("password"))
                .build();

        setupBucket(minioClient);
        setupBucketPolicy(minioClient);

        return minioClient;
    }

    private void setupBucket(MinioClient minioClient) {
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucket().get("name"))
                    .build();

            if (!minioClient.bucketExists(bucketExistsArgs)) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucket().get("name"))
                        .build();
                minioClient.makeBucket(makeBucketArgs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bucket", e);
        }
    }

    private void setupBucketPolicy(MinioClient minioClient) {
        try {
            String bucketPolicy = classpathFileService.getContent("static/bucket-policy.json");
            SetBucketPolicyArgs setBucketPolicyArgs = SetBucketPolicyArgs.builder()
                    .bucket(minioProperties.getBucket().get("name"))
                    .config(bucketPolicy)
                    .build();
            minioClient.setBucketPolicy(setBucketPolicyArgs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set bucket policy", e);
        }
    }
}