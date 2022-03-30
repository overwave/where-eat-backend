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
    private final ConcurrentHashMap<FileDescriptor, CompletableFuture<LocalFile>> downloadQueue =
            new ConcurrentHashMap<>();

    private final FeedService feedService;

    private final FileRepository fileRepository;

    public CompletableFuture<LocalFile> getFile(FileDescriptor fileDescriptor) {
        if (fileDescriptor.isDownloaded()) {
            return CompletableFuture.completedFuture(fileDescriptor.file().local);
        }

        return fileRepository.findById(fileDescriptor.fileId())
                .map(this::mapToLocalFile)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> getDownloading(fileDescriptor));
    }

    private LocalFile mapToLocalFile(File file) {
        return new LocalFile(file.getPath(), true, true, false, true, 0, file.getSize(), file.getSize());
    }

    private CompletableFuture<LocalFile> getDownloading(FileDescriptor file) {
        return downloadQueue.computeIfAbsent(file, this::startDownloading);
    }

    private CompletableFuture<LocalFile> startDownloading(FileDescriptor file) {
        feedService.sendAsynchronously(new AddFileToDownloads(file.fileId(), file.chatId(), file.messageId(), 1));
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
            FileDescriptor descriptor = new FileDescriptor(file.id, -1, -1, file);
            fileRepository.save(new File(file.id, localFile.path, localFile.downloadedSize));
            downloadQueue.remove(descriptor).complete(localFile);
        } else if (localFile.isDownloadingActive) {
            System.out.println((localFile.downloadedSize * 100 / file.expectedSize) + "% downloaded");
        }
    }
}
