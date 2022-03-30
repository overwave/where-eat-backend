package dev.overwave.whereeat.feed;

import dev.overwave.whereeat.file.FileDescriptor;
import dev.overwave.whereeat.file.FileDownloadService;
import it.tdlight.jni.TdApi;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class ParserService {
    private final FeedService feedService;

    private final FileDownloadService fileDownloadService;

    @Value("${whereeat.client.chat-name}")
    private String chatName;

    @PostConstruct
    private void downloadPhotos() {
        TdApi.Chats chats = feedService.sendSynchronously(new TdApi.GetChats(new TdApi.ChatListMain(), 500));
        TdApi.Chat chat = Arrays.stream(chats.chatIds)
                .mapToObj(id -> feedService.sendSynchronously(new TdApi.GetChat(id)))
                .filter(ch -> chatName.equals(ch.title))
                .findFirst()
                .orElseThrow();

        long lastMessage = 0;
        for (int i = 0; i < 10; i++) {
            TdApi.Messages messages = feedService.sendSynchronously(new TdApi.GetChatHistory(chat.id, lastMessage, 0, 10, false));

            for (TdApi.Message message : messages.messages) {
                if (message.content instanceof TdApi.MessageText messageText) {
                    System.out.println(messageText.text.text);
                } else if (message.content instanceof TdApi.MessagePhoto messagePhoto) {
                    TdApi.LocalFile localFile = Arrays.stream(messagePhoto.photo.sizes)
                            .map(photoSize -> photoSize.photo)
                            .max(Comparator.comparingInt(file -> file.size))
                            .map(file -> new FileDescriptor(file.id, message.id, message.chatId, file))
                            .map(fileDownloadService::getFile)
                            .map(CompletableFuture::join)
                            .orElseThrow();

                    System.out.println(localFile);

                    System.out.println(messagePhoto.caption.text);
                }
            }
            lastMessage = messages.messages[messages.totalCount - 1].id;
        }
    }
}
