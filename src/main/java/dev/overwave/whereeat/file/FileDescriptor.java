package dev.overwave.whereeat.file;

import java.util.Objects;

import static it.tdlight.jni.TdApi.File;

public record FileDescriptor(File file, long messageId, long chatId) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDescriptor that = (FileDescriptor) o;
        return file.id == that.file.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file.id);
    }
}
