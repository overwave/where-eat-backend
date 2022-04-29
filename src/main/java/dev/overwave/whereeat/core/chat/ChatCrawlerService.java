package dev.overwave.whereeat.core.chat;

import dev.overwave.whereeat.core.media.FileDescriptor;
import dev.overwave.whereeat.core.media.FileDownloadService;
import dev.overwave.whereeat.core.media.Media;
import dev.overwave.whereeat.core.message.AttachmentType;
import dev.overwave.whereeat.core.message.Message;
import dev.overwave.whereeat.core.message.MessageRepository;
import dev.overwave.whereeat.core.message.MessageType;
import dev.overwave.whereeat.core.message.TextAttachment;
import dev.overwave.whereeat.core.util.StringUtils;
import it.tdlight.jni.TdApi;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static it.tdlight.jni.TdApi.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatCrawlerService {
    private final ChatReaderService chatReaderService;

    private final FileDownloadService fileDownloadService;

    private final MessageRepository messageRepository;

    @Transactional
    public void crawl() {
        List<TdApi.Message> messages = chatReaderService.getNewMessages();
        if (messages.isEmpty()) return;

        for (TdApi.Message message : messages) {
            MessageContent content = message.content;
            String text;
            MessageType messageType;
            Media media;
            List<TextAttachment> attachments;

            if (content instanceof MessageText messageText) {
                text = messageText.text.text;
                attachments = extractAttachments(messageText.text);
                messageType = MessageType.TEXT;
                media = null;
            } else if (content instanceof MessagePhoto photo) {
                text = photo.caption.text;
                attachments = extractAttachments(photo.caption);
                messageType = MessageType.PHOTO;
                media = getPhoto(photo, message);
            } else if (content instanceof MessageVideo video) {
                text = video.caption.text;
                attachments = extractAttachments(video.caption);
                messageType = MessageType.VIDEO;
                media = getVideo(video, message);
            } else if (content instanceof MessageAnimation animation) {
                text = animation.caption.text;
                attachments = extractAttachments(animation.caption);
                messageType = MessageType.ANIMATION;
                media = getAnimation(animation, message);
            } else if (content instanceof MessagePoll poll) {
                log.warn("Poll {} skipped.", StringUtils.trimWithEllipsis(poll.poll.question, 20));
                continue;
            } else {
                log.error("Unknown message type: {}.", message.content);
                continue;
            }

            String messageGroupId = message.mediaAlbumId == 0 ? "T-" + message.id : "M-" + message.mediaAlbumId;
            Message newMessage = new Message(message.id, text, attachments, messageType,
                    Instant.ofEpochSecond(message.date), media, messageGroupId);
            attachments.forEach(a -> a.setMessage(newMessage));

            messageRepository.save(newMessage);
        }
        log.info("Crawled {} messages [{} -> {}].", messages.size(), messages.get(0).id, messages.get(messages.size() - 1).id);
    }

    private List<TextAttachment> extractAttachments(FormattedText formattedText) {
        List<TextAttachment> attachments = new ArrayList<>();

        for (TextEntity entity : formattedText.entities) {
            if (entity.type instanceof TextEntityTypeTextUrl url) {
                attachments.add(new TextAttachment(entity.offset, entity.length, AttachmentType.NAMED_URL, url.url));
            } else if (entity.type instanceof TextEntityTypeUrl) {
                attachments.add(new TextAttachment(entity.offset, entity.length, AttachmentType.URL));
            } else if (entity.type instanceof TextEntityTypeBold) {
                attachments.add(new TextAttachment(entity.offset, entity.length, AttachmentType.BOLD));
            } else if (entity.type instanceof TextEntityTypeStrikethrough) {
                attachments.add(new TextAttachment(entity.offset, entity.length, AttachmentType.STRIKETHROUGH));
            } else if (entity.type instanceof TextEntityTypeHashtag) {
                attachments.add(new TextAttachment(entity.offset, entity.length, AttachmentType.HASHTAG));
            } else if (entity.type instanceof TextEntityTypeMention) {
                attachments.add(new TextAttachment(entity.offset, entity.length, AttachmentType.MENTION));
            } else {
                log.warn("Unexpected attachment: {}", entity);
            }
        }

        return attachments;
    }

    private Media getPhoto(MessagePhoto photo, TdApi.Message message) {
        return Arrays.stream(photo.photo.sizes)
                .map(photoSize -> photoSize.photo)
                .max(Comparator.comparingInt(file -> file.size))
                .map(file -> new FileDescriptor(file, message.id, message.chatId))
                .map(fileDownloadService::getMedia)
                .map(CompletableFuture::join)
                .orElseThrow();
    }

    private Media getVideo(MessageVideo messageVideo, TdApi.Message message) {
        FileDescriptor descriptor = new FileDescriptor(messageVideo.video.video, message.id, message.chatId);
        return fileDownloadService.getMedia(descriptor).join();
    }

    private Media getAnimation(MessageAnimation messageAnimation, TdApi.Message message) {
        FileDescriptor descriptor = new FileDescriptor(messageAnimation.animation.animation, message.id, message.chatId);
        return fileDownloadService.getMedia(descriptor).join();
    }
}
