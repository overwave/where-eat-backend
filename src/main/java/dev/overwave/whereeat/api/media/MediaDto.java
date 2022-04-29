package dev.overwave.whereeat.api.media;

import dev.overwave.whereeat.core.message.MessageType;
import org.springframework.data.util.Pair;

public record MediaDto(String name, int width, int height, MessageType type) {
    public MediaDto withSize(Pair<Integer, Integer> size) {
        return new MediaDto(name, size.getFirst(), size.getSecond(), type);
    }
}
