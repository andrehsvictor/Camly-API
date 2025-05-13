package andrehsvictor.camly.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class ClasspathFileService {

    public String getContent(String filePath) {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file from classpath: " + filePath, e);
        }
    }
}