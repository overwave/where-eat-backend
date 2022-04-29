package dev.overwave.whereeat.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.io.InputStream;

@UtilityClass
public class FileUtils {
    public static byte[] readBlob(String path) throws IOException {
        FileSystemResource fileSystemResource = new FileSystemResource(path);

        try (InputStream inputStream = fileSystemResource.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    public static String getFilename(String path) {
        FileSystemResource fileSystemResource = new FileSystemResource(path);
        return fileSystemResource.getFile().getName();
    }
}
