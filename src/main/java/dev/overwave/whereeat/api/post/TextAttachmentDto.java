package dev.overwave.whereeat.api.post;

import dev.overwave.whereeat.core.message.AttachmentType;

public record TextAttachmentDto(AttachmentType type, int offset, int length, String value) {
}
