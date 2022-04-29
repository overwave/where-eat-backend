package dev.overwave.whereeat.api.post;

import dev.overwave.whereeat.api.media.MediaDto;

import java.util.List;

public record PostDto(long id, String text, List<TextAttachmentDto> textAttachments, List<MediaDto> media) {
}
