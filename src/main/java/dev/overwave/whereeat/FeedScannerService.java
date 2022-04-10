package dev.overwave.whereeat;

import dev.overwave.whereeat.chat.ChatService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedScannerService {
    private final ChatService chatService;

    @PostConstruct
    public void runScanning() {
        chatService.getHistory();
    }
}
