package andrehsvictor.camly.image;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import andrehsvictor.camly.exception.BadRequestException;
import andrehsvictor.camly.image.dto.ImageDto;
import andrehsvictor.camly.minio.MinioService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final MinioService minioService;

    public ImageDto upload(MultipartFile file) {
        validateImage(file);
        return new ImageDto(minioService.uploadFile(file));
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty.");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestException("Invalid file type. Only image files are allowed.");
        }
    }
}