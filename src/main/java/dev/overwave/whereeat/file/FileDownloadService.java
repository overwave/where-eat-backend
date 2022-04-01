package dev.overwave.whereeat.file;

import dev.overwave.whereeat.feed.FeedService;
import it.tdlight.jni.TdApi;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static it.tdlight.jni.TdApi.AddFileToDownloads;
import static it.tdlight.jni.TdApi.LocalFile;
import static it.tdlight.jni.TdApi.UpdateFile;

@Service
@RequiredArgsConstructor
public class FileDownloadService {
    private final ConcurrentHashMap<FileDescriptor, CompletableFuture<FileDto>> downloadQueue =
            new ConcurrentHashMap<>();

    private final FeedService feedService;

    private final FileRepository fileRepository;

    public CompletableFuture<FileDto> getFile(FileDescriptor descriptor) {
        if (descriptor.file().local.isDownloadingCompleted) {
            File savedFile = fileRepository.save(
                    new File(descriptor.file().id, descriptor.file().local.path, descriptor.file().size));
            return CompletableFuture.completedFuture(mapToDto(savedFile));
        }

        return fileRepository.findById(descriptor.file().id)
                .map(this::mapToDto)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> getDownloading(descriptor));
    }

    private FileDto mapToDto(File file) {
        return new FileDto(file.getId(), file.getPath(), file.getSize());
    }

    private CompletableFuture<FileDto> getDownloading(FileDescriptor file) {
        return downloadQueue.computeIfAbsent(file, this::startDownloading);
    }

    private CompletableFuture<FileDto> startDownloading(FileDescriptor file) {
        feedService.sendAsynchronously(new AddFileToDownloads(file.file().id, file.chatId(), file.messageId(), 1));
        return new CompletableFuture<>();
    }

    @PostConstruct
    private void attachUploadHandler() {
        feedService.addUpdateHandler(UpdateFile.class, this::fileHandler);
    }

    private void fileHandler(UpdateFile update) {
        TdApi.File file = update.file;
        LocalFile localFile = file.local;
        if (localFile.isDownloadingCompleted) {
            FileDescriptor descriptor = new FileDescriptor(file, -1, -1);
            File downloadedFile = fileRepository.save(new File(file.id, localFile.path, localFile.downloadedSize));
            downloadQueue.remove(descriptor).complete(mapToDto(downloadedFile));
        } else if (localFile.isDownloadingActive) {
            System.out.println((localFile.downloadedSize * 100 / file.expectedSize) + "% downloaded");
        }
    }
}
