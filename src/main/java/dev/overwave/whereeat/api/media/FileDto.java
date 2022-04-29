package dev.overwave.whereeat.api.media;

import dev.overwave.whereeat.core.message.MessageType;

public record FileDto(String name, MessageType messageType, byte[] data) {
}
