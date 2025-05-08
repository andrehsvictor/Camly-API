package andrehsvictor.camly.image;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import andrehsvictor.camly.image.dto.ImageDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/api/v1/images")
    public ImageDto upload(MultipartFile file) {
        return imageService.upload(file);
    }

}
