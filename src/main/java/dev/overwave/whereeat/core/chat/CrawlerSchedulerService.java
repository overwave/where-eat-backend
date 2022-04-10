package dev.overwave.whereeat.core.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrawlerSchedulerService {
    private final ChatCrawlerService chatCrawlerService;

    @Scheduled(cron = "${whereeat.client.crawl-cron}")
    public void crawlFeed() {
        chatCrawlerService.crawl();
    }
}
