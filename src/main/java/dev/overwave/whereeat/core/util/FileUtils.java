package dev.overwave.whereeat.core.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.util.Pair;

import java.io.File;
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

    @SneakyThrows
    public static Pair<Integer, Integer> getMediaDimensions(String path) {
        File file = new File(path);
        ImageFormat format = Imaging.guessFormat(file);

        if (format == ImageFormats.UNKNOWN) {
            Picture picture = FrameGrab.getFrameFromFile(file, 0);
            return Pair.of(picture.getWidth(), picture.getHeight());
        } else {
            ImageInfo info = Imaging.getImageInfo(file);
            return Pair.of(info.getWidth(), info.getHeight());
        }
    }
}
