package dev.overwave.whereeat.api.media;

import dev.overwave.whereeat.core.message.MessageType;

public record MediaDto(String name, double width, double height, MessageType type) {
}
