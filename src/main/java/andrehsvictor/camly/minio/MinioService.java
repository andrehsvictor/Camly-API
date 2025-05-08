package andrehsvictor.camly.minio;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    public String uploadFile(MultipartFile file) {
        try {
            String objectName = generateObjectName(file.getOriginalFilename());

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            return String.format("%s/%s/%s", minioProperties.getEndpoint(), minioProperties.getBucketName(),
                    objectName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    private String generateObjectName(String originalFilename) {
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String timestamp = String.valueOf(System.currentTimeMillis());
        return timestamp + fileExtension;
    }
}