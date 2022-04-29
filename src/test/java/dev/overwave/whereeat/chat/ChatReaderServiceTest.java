package dev.overwave.whereeat.chat;

import dev.overwave.whereeat.core.chat.ChatReaderService;
import dev.overwave.whereeat.core.chat.ChatService;
import dev.overwave.whereeat.core.message.ScannedRange;
import dev.overwave.whereeat.core.message.ScannedRangeRepository;
import dev.overwave.whereeat.util.Factory;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static dev.overwave.whereeat.core.chat.ChatReaderService.DEFAULT_MESSAGES_BATCH_SIZE;
import static dev.overwave.whereeat.util.VerificationMode.once;
import static it.tdlight.jni.TdApi.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ChatReaderServiceTest {

    private static final long LAST_MESSAGE_ID = 1001L;
    private static final long TEST_CHAT_ID = 1L;
    private static final String TEST_CHAT_TITLE = "TEST_CHAT_TITLE";

    private final ChatReaderService chatReaderService;

    @MockBean
    private final ScannedRangeRepository scannedRangeRepository;

    @MockBean
    private final ChatService chatService;

    @BeforeEach
    void beforeEachSetup() {
        Message lastMessage = new Message();
        lastMessage.id = LAST_MESSAGE_ID;

        Chat chat = new Chat();
        chat.id = TEST_CHAT_ID;
        chat.title = TEST_CHAT_TITLE;
        chat.lastMessage = lastMessage;

        when(chatService.sendSynchronously(any(GetChats.class)))
                .thenReturn(new TdApi.Chats(1, new long[]{TEST_CHAT_ID}));
        when(chatService.sendSynchronously(any(GetChat.class)))
                .thenReturn(chat);

        when(scannedRangeRepository.findTwoLastRanges()).thenCallRealMethod();
    }

    /*

    [_________]

   + ###

    [###______]

     */
    @Test
    void testFirstRead() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of());
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID, -1)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 2));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages)
                .isEqualTo(Factory.messages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 2));
        verify(scannedRangeRepository, never()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(0, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 2));
    }

    /*

    [XXX______]

   +    ###

    [XXX###___]

     */
    @Test
    void testSecondRead() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of(new ScannedRange(0, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 5)));
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID - 5, 0)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID - 6, LAST_MESSAGE_ID - 7, LAST_MESSAGE_ID - 8));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages)
                .isEqualTo(Factory.messages(LAST_MESSAGE_ID - 6, LAST_MESSAGE_ID - 7, LAST_MESSAGE_ID - 8));
        verify(scannedRangeRepository, never()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(0, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 8));
    }

    /*

    [______XXX]

   + ###

    [###___XXX]

     */
    @Test
    void testReadWithAlreadyLongTimeAgoReadRange() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of(new ScannedRange(0, LAST_MESSAGE_ID - 100, LAST_MESSAGE_ID - 200)));
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID, -1)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 2));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages)
                .isEqualTo(Factory.messages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 2));
        verify(scannedRangeRepository, never()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(1, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 2));
    }

    /*

    [_XXX_____]

   + ###

    [#XXX_____]

     */
    @Test
    void testReadWithAlreadyRecentlyReadRange() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of(new ScannedRange(0, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 100)));
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID, -1)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 2));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages)
                .isEqualTo(Factory.messages(LAST_MESSAGE_ID));
        verify(scannedRangeRepository, never()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(0, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 100));
    }

    /*

    [__XXX____]

   + ###

    [##XXX____]

     */
    @Test
    void testReadWithAlreadyRecentlyReadRangeSameBorder() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of(new ScannedRange(0, LAST_MESSAGE_ID - 2, LAST_MESSAGE_ID - 100)));
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID, -1)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 2));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages)
                .isEqualTo(Factory.messages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1));
        verify(scannedRangeRepository, never()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(0, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 100));
    }

    /*

    [___XXX___]

   + ###

    [###XXX___]

     */
    @Test
    void testReadWithAlreadyRecentlyReadRangeCloseBorder() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of(new ScannedRange(0, LAST_MESSAGE_ID - 3, LAST_MESSAGE_ID - 100)));
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID, -1)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 2));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages)
                .isEqualTo(Factory.messages(LAST_MESSAGE_ID, LAST_MESSAGE_ID - 1, LAST_MESSAGE_ID - 2));
        verify(scannedRangeRepository, never()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(1, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 2));
    }

    /*

        |
        v нулевой диапазон
    [XXXXXX____]

   +    ###

    [XXXXXX____]   без пробела

     */
    @Test
    void testReadEmptyRange() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of(
                        new ScannedRange(1, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 50),
                        new ScannedRange(0, LAST_MESSAGE_ID - 51, LAST_MESSAGE_ID - 100)
                ));
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID - 50, 0)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID - 51, LAST_MESSAGE_ID - 52, LAST_MESSAGE_ID - 53));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages).isEmpty();
        verify(scannedRangeRepository, once()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(0, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 100));
    }


    /*

    [XXX____XXX]

   +    ###

    [XXX##__XXX]

     */
    @Test
    void testReadSecondRangeBeforeThird() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of(
                        new ScannedRange(1, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 3),
                        new ScannedRange(0, LAST_MESSAGE_ID - 10, LAST_MESSAGE_ID - 100)
                ));
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID - 3, 0)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID - 4, LAST_MESSAGE_ID - 5, LAST_MESSAGE_ID - 6));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages)
                .isEqualTo(Factory.messages(LAST_MESSAGE_ID - 4, LAST_MESSAGE_ID - 5, LAST_MESSAGE_ID - 6));
        verify(scannedRangeRepository, never()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(1, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 6));
    }

    /*

    [XXX__XXX__]

   +    ###

    [XXX##XXX__]

     */
    @Test
    void testReadSecondRangeBeforeThirdWithOverlap() {
        when(scannedRangeRepository.findTop2ByOrderByOrdinalDesc())
                .thenReturn(List.of(
                        new ScannedRange(1, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 3),
                        new ScannedRange(0, LAST_MESSAGE_ID - 6, LAST_MESSAGE_ID - 100)
                ));
        when(chatService.sendSynchronously(getHistory(LAST_MESSAGE_ID - 3, 0)))
                .thenReturn(Factory.tdMessages(LAST_MESSAGE_ID - 4, LAST_MESSAGE_ID - 5, LAST_MESSAGE_ID - 6));

        List<Message> actualMessages = chatReaderService.getNewMessages();

        assertThat(actualMessages)
                .isEqualTo(Factory.messages(LAST_MESSAGE_ID - 4, LAST_MESSAGE_ID - 5));
        verify(scannedRangeRepository, once()).delete(any());
        ArgumentCaptor<ScannedRange> captor = ArgumentCaptor.forClass(ScannedRange.class);
        verify(scannedRangeRepository, once()).save(captor.capture());
        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new ScannedRange(0, LAST_MESSAGE_ID, LAST_MESSAGE_ID - 100));
    }

    private GetChatHistory getHistory(long lastMessageId, int offset) {
        return new GetChatHistory(TEST_CHAT_ID, lastMessageId, offset, DEFAULT_MESSAGES_BATCH_SIZE, false);
    }
}