package andrehsvictor.camly.image;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import andrehsvictor.camly.image.dto.ImageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image upload operations")
@SecurityRequirement(name = "bearer-jwt")
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "Upload an image", description = "Uploads an image file and returns a URL that can be used in posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file format or empty file", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content),
            @ApiResponse(responseCode = "413", description = "File too large - Maximum size exceeded", content = @Content)
    })
    @PostMapping(value = "/api/v1/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageDto upload(
            @Parameter(description = "Image file to upload (JPEG, PNG format)", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("file") MultipartFile file) {
        return imageService.upload(file);
    }
}