package dev.overwave.whereeat;

import dev.overwave.whereeat.feed.FeedService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedScannerService {
    private final FeedService feedService;

    @PostConstruct
    public void runScanning() {
        feedService.getHistory();
    }
}
