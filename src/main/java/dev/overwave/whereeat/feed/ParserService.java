package dev.overwave.whereeat.feed;

import dev.overwave.whereeat.file.File;
import dev.overwave.whereeat.file.FileDescriptor;
import dev.overwave.whereeat.file.FileDownloadService;
import dev.overwave.whereeat.file.FileDto;
import dev.overwave.whereeat.post.Post;
import dev.overwave.whereeat.post.PostRepository;
import dev.overwave.whereeat.util.StringUtils;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

import static it.tdlight.jni.TdApi.*;

@RequiredArgsConstructor
@Service
public class ParserService {
    private final FeedService feedService;

    private final FileDownloadService fileDownloadService;

    private final PostRepository postRepository;

    @Value("${whereeat.client.chat-name}")
    private String chatName;

    @PostConstruct
    private void downloadPhotos() {
        Chat chat = getChat();

        long lastMessage = 0;
        for (int i = 0; i < 10; i++) {
            Messages messages = feedService.sendSynchronously(new GetChatHistory(chat.id, lastMessage, 0, 10, false));

            for (Message message : messages.messages) {
                MessageContent content = message.content;
                String text = "";
                FileDto fileDto = null;

                if (content instanceof MessageText messageText) {
                    text = messageText.text.text;
                } else if (content instanceof MessagePhoto photo) {
                    text = photo.caption.text;
                    fileDto = getPhoto(photo, message);
                } else if (content instanceof MessageVideo video) {
                    text = video.caption.text;
                    fileDto = getVideo(video, message);
                } else if (content instanceof MessageAnimation animation) {
                    text = animation.caption.text;
                    fileDto = getAnimation(animation, message);
                } else if (content instanceof MessagePoll poll) {
                    System.out.println("Poll " + StringUtils.trimWithEllipsis(poll.poll.question, 20) + " skipped...");
                } else {
                    System.err.println("unknown message type: " + message);
                }

                File file = fileDto != null ? new File(fileDto.id(), fileDto.path(), fileDto.size()) : null;
                postRepository.save(new Post(text, file));
            }
            lastMessage = messages.messages[messages.totalCount - 1].id;
        }
    }

    private Chat getChat() {
        Chats chats = feedService.sendSynchronously(new GetChats(new ChatListMain(), 500));
        return Arrays.stream(chats.chatIds)
                .mapToObj(id -> feedService.sendSynchronously(new GetChat(id)))
                .filter(ch -> chatName.equals(ch.title))
                .findFirst()
                .orElseThrow();
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
