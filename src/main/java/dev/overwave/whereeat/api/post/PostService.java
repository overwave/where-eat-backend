package dev.overwave.whereeat.api.post;

import dev.overwave.whereeat.api.media.MediaDto;
import dev.overwave.whereeat.core.media.Media;
import dev.overwave.whereeat.core.message.Message;
import dev.overwave.whereeat.core.message.MessageRepository;
import dev.overwave.whereeat.core.message.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final MessageRepository messageRepository;

    public Page<PostDto> getPosts(Pageable pageable) {
        Page<String> groups = messageRepository.findGroups(pageable);

        return groups.map(this::mapGroups);
    }

    private PostDto mapGroups(String groupId) {
        List<Message> messagesGroup = messageRepository.findAllByGroupIdAndHiddenIsFalse(groupId);
        Message firstMessage = messagesGroup.get(0);

        List<TextAttachmentDto> attachments = new ArrayList<>();
        List<MediaDto> media = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (Message message : messagesGroup) {
            message.getTextAttachments().stream()
                    .map(a -> new TextAttachmentDto(a.getType(), builder.length() + a.getOffset(), a.getOffset(), a.getValue()))
                    .forEach(attachments::add);

            if (message.getType() != MessageType.TEXT) {
                Media m = message.getMedia();
                media.add(new MediaDto(m.getId(), m.getWidth(), m.getHeight(), message.getType()));
            }

            builder.append(message.getText());
        }
        return new PostDto(firstMessage.getMessageId(), builder.toString(), attachments, media);
    }
}
