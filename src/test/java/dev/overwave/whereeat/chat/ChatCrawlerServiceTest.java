package dev.overwave.whereeat.chat;

import dev.overwave.whereeat.core.chat.ChatCrawlerService;
import dev.overwave.whereeat.core.chat.ChatService;
import dev.overwave.whereeat.core.media.FileDownloadService;
import dev.overwave.whereeat.core.media.MediaRepository;
import dev.overwave.whereeat.core.message.Message;
import dev.overwave.whereeat.core.message.MessageRepository;
import dev.overwave.whereeat.core.message.ScannedRange;
import dev.overwave.whereeat.core.message.ScannedRangeRepository;
import dev.overwave.whereeat.util.Factory;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static it.tdlight.jni.TdApi.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ChatCrawlerServiceTest {

    private static final long LAST_MESSAGE_ID = 1001L;
    private static final long TEST_CHAT_ID = 1L;
    private static final String TEST_CHAT_TITLE = "TEST_CHAT_TITLE";

    private final ChatCrawlerService chatCrawlerService;

    private final ScannedRangeRepository scannedRangeRepository;

    private final MessageRepository messageRepository;

    private final MediaRepository mediaRepository;

    @MockBean
    private ChatService chatService;

    @SpyBean
    private FileDownloadService fileDownloadService;

    @BeforeEach
    void beforeEachSetup() {
        TdApi.Message lastMessage = new TdApi.Message();
        lastMessage.id = LAST_MESSAGE_ID;

        Chat chat = new Chat();
        chat.id = TEST_CHAT_ID;
        chat.title = TEST_CHAT_TITLE;
        chat.lastMessage = lastMessage;

        Messages messages = new Messages();
        messages.messages = Factory.videoMessages(1, 2, 3, 4, 5).toArray(TdApi.Message[]::new);

        when(chatService.sendSynchronously(any(GetChats.class)))
                .thenReturn(new Chats(1, new long[]{TEST_CHAT_ID}));
        when(chatService.sendSynchronously(any(GetChat.class)))
                .thenReturn(chat);
        when(chatService.sendSynchronously(any(GetChatHistory.class)))
                .thenReturn(messages);

        scannedRangeRepository.deleteAll();
        messageRepository.deleteAll();
        mediaRepository.deleteAll();
    }

    @Test
    void testFirstChatCrawl() {
        chatCrawlerService.crawl();

        List<ScannedRange> scannedRanges = scannedRangeRepository.findAll();
        List<Message> messages = messageRepository.findAll();

        assertThat(scannedRanges)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(new ScannedRange(0, 1, 5)));
        assertThat(messages)
                .hasSize(5);
    }

    @Test
    void testRangeNotScannedIfFails() {
        RuntimeException expectedException = new RuntimeException();

        doCallRealMethod()
                .doCallRealMethod()
                .doCallRealMethod()
                .doCallRealMethod()
                .doThrow(expectedException)
                .when(fileDownloadService)
                .getMedia(any());

        assertThatThrownBy(chatCrawlerService::crawl)
                .isEqualTo(expectedException);

        assertThat(scannedRangeRepository.findAll()).isEmpty();
        assertThat(messageRepository.findAll()).isEmpty();
        assertThat(mediaRepository.findAll()).isEmpty();
    }
}