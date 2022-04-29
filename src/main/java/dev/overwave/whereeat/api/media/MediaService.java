package dev.overwave.whereeat.api.media;

import dev.overwave.whereeat.core.media.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;

    public byte[] getFile(String fileName) {
        return mediaRepository.findById(fileName)
                .orElseThrow()
                .getData();
    }
}
