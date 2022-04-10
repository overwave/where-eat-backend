package dev.overwave.whereeat.chat;

import dev.overwave.whereeat.post.LastRanges;
import dev.overwave.whereeat.post.ScannedRange;
import dev.overwave.whereeat.post.ScannedRangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static it.tdlight.jni.TdApi.*;

@Service
@RequiredArgsConstructor
public class ChatReaderService {
    public static final int DEFAULT_MESSAGES_BATCH_SIZE = 10;
    private final ChatService chatService;

    private final ScannedRangeRepository scannedRangeRepository;

    @Value("${whereeat.client.chat-name}")
    private String chatName;

    private Chat getChat() {
        GetChats function = new GetChats(new ChatListMain(), 500);
        Chats chats = chatService.sendSynchronously(function);
        return Arrays.stream(chats.chatIds)
                .mapToObj(id -> chatService.sendSynchronously(new GetChat(id)))
                .filter(ch -> chatName.equals(ch.title))
                .findFirst()
                .orElseThrow();
    }

    public List<Message> getNewMessages() {
        Chat chat = getChat();
        long lastMessageId = chat.lastMessage.id;

        LastRanges lastRanges = scannedRangeRepository.findTwoLastRanges();

        if (lastRanges.isEmpty()) {
            return readFirstRange(chat, lastMessageId);
        }

        ScannedRange firstRange = lastRanges.getFirst();
        if (firstRange.getMessageIdFrom() != lastMessageId) {
            Messages messages = readMessages(chat, lastMessageId, 0);
            Integer borderIndex = findRangeBorderIndex(messages.messages, firstRange.getMessageIdFrom());

            if (borderIndex == null) {
                return readRangeBeforeFirst(firstRange, messages);
            } else if (borderIndex == 0) {
                throw new IllegalStateException("firstRange.from should be != lastMessageId");
            } else {
                return readRangeBeforeFirstWithOverlap(firstRange, messages, borderIndex);
            }
        } else {
            return tryReadSecondRange(chat, lastRanges);
        }
    }

    private List<Message> readFirstRange(Chat chat, long messageId) {
        Messages messages = readMessages(chat, messageId, 0);
        List<Message> newMessages = Arrays.asList(messages.messages);

        Message firstMessage = newMessages.get(0);
        Message lastMessage = newMessages.get(newMessages.size() - 1);
        ScannedRange newRange = new ScannedRange(0, firstMessage.id, lastMessage.id);

        scannedRangeRepository.save(newRange);
        return newMessages;
    }

    private Messages readMessages(Chat chat, long messageId, int offset) {
        return chatService.sendSynchronously(
                new GetChatHistory(chat.id, messageId, offset - 1, DEFAULT_MESSAGES_BATCH_SIZE, false));
    }

    private List<Message> readRangeBeforeFirstWithOverlap(ScannedRange firstRange, Messages messages, int borderIndex) {
        List<Message> newMessages = Arrays.asList(messages.messages).subList(0, borderIndex);

        Message firstMessage = newMessages.get(0);

        ScannedRange mergedRange = new ScannedRange(firstRange.getOrdinal(), firstMessage.id, firstRange.getMessageIdTo());

        scannedRangeRepository.delete(firstRange);
        scannedRangeRepository.save(mergedRange);
        return newMessages;
    }

    private List<Message> readRangeBeforeFirst(ScannedRange firstRange, Messages messages) {
        List<Message> newMessages = Arrays.asList(messages.messages);

        Message firstMessage = newMessages.get(0);
        Message lastMessage = newMessages.get(newMessages.size() - 1);
        ScannedRange newRange = new ScannedRange(firstRange.getOrdinal() + 1, firstMessage.id, lastMessage.id);

        scannedRangeRepository.save(newRange);
        return newMessages;
    }

    private List<Message> tryReadSecondRange(Chat chat, LastRanges lastRanges) {
        ScannedRange firstRange = lastRanges.getFirst();
        ScannedRange secondRange = lastRanges.getSecond();
        Messages messages = readMessages(chat, firstRange.getMessageIdTo(), 1);

        if (secondRange == null) {
            return saveSecondRangeAfterFirst(firstRange, messages);
        }

        Integer borderIndex = findRangeBorderIndex(messages.messages, secondRange.getMessageIdFrom());

        if (borderIndex == null) {
            return saveSecondRangeBeforeThird(firstRange, secondRange, messages);
        } else if (borderIndex == 0) {
            return mergeTwoRangesAndReturnEmpty(firstRange, secondRange);
        } else {
            return saveSecondRangeBeforeThirdWithOverlap(firstRange, secondRange, borderIndex, messages);
        }
    }

    private List<Message> saveSecondRangeAfterFirst(ScannedRange firstRange, Messages messages) {
        List<Message> newMessages = Arrays.asList(messages.messages);
        Message lastMessage = newMessages.get(newMessages.size() - 1);

        firstRange.setMessageIdTo(lastMessage.id);

        scannedRangeRepository.save(firstRange);

        return newMessages;
    }

    private List<Message> mergeTwoRangesAndReturnEmpty(ScannedRange firstRange, ScannedRange secondRange) {
        ScannedRange mergedRange = new ScannedRange(secondRange.getOrdinal(), firstRange.getMessageIdFrom(), secondRange.getMessageIdTo());

        scannedRangeRepository.delete(firstRange);
        scannedRangeRepository.delete(secondRange);
        scannedRangeRepository.save(mergedRange);

        return List.of();
    }

    private List<Message> saveSecondRangeBeforeThird(ScannedRange firstRange, ScannedRange secondRange, Messages messages) {
        List<Message> newMessages = Arrays.asList(messages.messages);

        Message lastMessage = newMessages.get(newMessages.size() - 1);
        ScannedRange mergedRange = new ScannedRange(secondRange.getOrdinal(), firstRange.getMessageIdFrom(), lastMessage.id);

        scannedRangeRepository.delete(firstRange);
        scannedRangeRepository.save(mergedRange);
        return newMessages;
    }

    private List<Message> saveSecondRangeBeforeThirdWithOverlap(ScannedRange firstRange, ScannedRange secondRange,
                                                                int borderIndex, Messages messages) {
        List<Message> newMessages = Arrays.asList(messages.messages).subList(0, borderIndex);

        ScannedRange mergedRange = new ScannedRange(secondRange.getOrdinal(), firstRange.getMessageIdFrom(), secondRange.getMessageIdTo());

        scannedRangeRepository.delete(firstRange);
        scannedRangeRepository.delete(secondRange);
        scannedRangeRepository.save(mergedRange);
        return newMessages;
    }

    private Integer findRangeBorderIndex(Message[] messages, long fromId) {
        for (int i = 0; i < messages.length; i++) {
            if (messages[i].id == fromId) {
                return i;
            }
        }
        return null;
    }
}
