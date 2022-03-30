package dev.overwave.whereeat.file;

import java.util.Objects;

import static it.tdlight.jni.TdApi.File;

public record FileDescriptor(int fileId, long messageId, long chatId, File file) {
    public boolean isDownloaded() {
        return file.local.isDownloadingCompleted && !file.local.path.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDescriptor that = (FileDescriptor) o;
        return fileId == that.fileId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId);
    }
}
