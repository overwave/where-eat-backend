package dev.overwave.whereeat.core.media;

import dev.overwave.whereeat.core.chat.ChatService;
import dev.overwave.whereeat.core.util.FileUtils;
import it.tdlight.jni.TdApi;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static it.tdlight.jni.TdApi.AddFileToDownloads;
import static it.tdlight.jni.TdApi.LocalFile;
import static it.tdlight.jni.TdApi.UpdateFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService {
    private final ConcurrentHashMap<FileDescriptor, CompletableFuture<Media>> downloadQueue =
            new ConcurrentHashMap<>();

    private final ChatService chatService;

    private final MediaRepository mediaRepository;

    @SneakyThrows
    public CompletableFuture<Media> getMedia(FileDescriptor descriptor) {
        TdApi.File file = descriptor.file();
        if (file.local.isDownloadingCompleted) {
            Media media = saveFile(file.local.path, file.remote.uniqueId);

            return CompletableFuture.completedFuture(media);
        }

        return mediaRepository.findById(file.remote.uniqueId)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> getDownloading(descriptor));
    }

    private CompletableFuture<Media> getDownloading(FileDescriptor file) {
        return downloadQueue.computeIfAbsent(file, this::startDownloading);
    }

    private CompletableFuture<Media> startDownloading(FileDescriptor file) {
        chatService.sendAsynchronously(new AddFileToDownloads(file.file().id, file.chatId(), file.messageId(), 1));
        return new CompletableFuture<>();
    }

    @PostConstruct
    private void attachUploadHandler() {
        chatService.addUpdateHandler(UpdateFile.class, this::fileHandler);
    }

    private void fileHandler(UpdateFile update) {
        TdApi.File file = update.file;
        LocalFile localFile = file.local;
        if (localFile.isDownloadingCompleted) {
            FileDescriptor descriptor = new FileDescriptor(file, -1, -1);
            Media media = saveFile(localFile.path, file.remote.uniqueId);

            downloadQueue.remove(descriptor).complete(media);
        } else if (localFile.isDownloadingActive) {
            log.trace("{}% downloaded...", localFile.downloadedSize * 100 / file.expectedSize);
        }
    }

    @SneakyThrows
    private Media saveFile(String path, String uniqueId) {
        byte[] blob = FileUtils.readBlob(path);
        String filename = FileUtils.getFilename(path);
        Pair<Integer, Integer> size = FileUtils.getMediaDimensions(path);

        return mediaRepository.save(new Media(uniqueId + filename, size.getFirst(), size.getSecond(), blob));
    }
}
