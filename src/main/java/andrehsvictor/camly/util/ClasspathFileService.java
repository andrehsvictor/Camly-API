package andrehsvictor.camly.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ClasspathFileService {

    public String getFileContent(String filePath) {
        try {
            Path path = new ClassPathResource(filePath).getFile().toPath();
            return new String(Files.readString(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file from classpath: " + filePath, e);
        }
    }
}
