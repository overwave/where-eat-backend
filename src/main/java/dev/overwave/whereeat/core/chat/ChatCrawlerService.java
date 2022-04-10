package dev.overwave.whereeat.core.chat;

import dev.overwave.whereeat.core.file.FileDownloadService;
import dev.overwave.whereeat.core.util.StringUtils;
import dev.overwave.whereeat.core.file.File;
import dev.overwave.whereeat.core.file.FileDescriptor;
import dev.overwave.whereeat.core.file.FileDto;
import dev.overwave.whereeat.core.post.Post;
import dev.overwave.whereeat.core.post.PostRepository;
import dev.overwave.whereeat.core.post.PostType;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final PostRepository postRepository;

    @Transactional
    public void crawl() {
        List<Message> messages = chatReaderService.getNewMessages();
        if (messages.isEmpty()) return;

        for (Message message : messages) {
            MessageContent content = message.content;
            String text;
            PostType postType;
            FileDto fileDto;

            if (content instanceof MessageText messageText) {
                text = messageText.text.text;
                postType = PostType.TEXT;
                fileDto = null;
            } else if (content instanceof MessagePhoto photo) {
                text = photo.caption.text;
                postType = PostType.PHOTO;
                fileDto = getPhoto(photo, message);
            } else if (content instanceof MessageVideo video) {
                text = video.caption.text;
                postType = PostType.VIDEO;
                fileDto = getVideo(video, message);
            } else if (content instanceof MessageAnimation animation) {
                text = animation.caption.text;
                postType = PostType.ANIMATION;
                fileDto = getAnimation(animation, message);
            } else if (content instanceof MessagePoll poll) {
                log.warn("Poll {} skipped.", StringUtils.trimWithEllipsis(poll.poll.question, 20));
                continue;
            } else {
                log.error("Unknown message type: {}.", message.content);
                continue;
            }

            File file = fileDto != null ? new File(fileDto.id(), fileDto.path(), fileDto.size()) : null;
            postRepository.save(new Post(message.id, text, postType, file));
        }
        log.info("Crawled {} messages [{} -> {}].", messages.size(), messages.get(0).id, messages.get(messages.size() - 1).id);
    }

    private FileDto getPhoto(MessagePhoto photo, Message message) {
        return Arrays.stream(photo.photo.sizes)
                .map(photoSize -> photoSize.photo)
                .max(Comparator.comparingInt(file -> file.size))
                .map(file -> new FileDescriptor(file, message.id, message.chatId))
                .map(fileDownloadService::getFile)
                .map(CompletableFuture::join)
                .orElseThrow();
    }

    private FileDto getVideo(MessageVideo messageVideo, Message message) {
        FileDescriptor descriptor = new FileDescriptor(messageVideo.video.video, message.id, message.chatId);
        return fileDownloadService.getFile(descriptor).join();
    }

    private FileDto getAnimation(MessageAnimation messageAnimation, Message message) {
        FileDescriptor descriptor = new FileDescriptor(messageAnimation.animation.animation, message.id, message.chatId);
        return fileDownloadService.getFile(descriptor).join();
    }
}
