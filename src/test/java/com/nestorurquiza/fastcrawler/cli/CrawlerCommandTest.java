package com.nestorurquiza.fastcrawler.cli;

import com.nestorurquiza.fastcrawler.model.CrawlRequest;
import com.nestorurquiza.fastcrawler.model.CrawlResult;
import com.nestorurquiza.fastcrawler.service.CrawlerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class CrawlerCommandTest {

    @Mock
    private CrawlerService crawlerService;

    @InjectMocks
    private CrawlerCommand crawlerCommand;

    @Test
    void shouldHandleValidArguments() {
        // Given
        String[] args = {"https://example.com", "--same-domain-only", "--max-concurrent", "3"};
        Set<String> urls = new HashSet<>();
        urls.add("https://example.com");
        
        CrawlResult mockResult = CrawlResult.builder()
                .visitedUrls(urls)
                .duration(Duration.ofSeconds(1))
                .totalPages(1)
                .build();
        
        when(crawlerService.crawl(any(CrawlRequest.class))).thenReturn(mockResult);

        // When
        crawlerCommand.run(args);

        // Then
        verify(crawlerService).crawl(any(CrawlRequest.class));
    }

    @Test
    void shouldHandleNoArguments() {
        // Given
        String[] args = {};

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            crawlerCommand.run(args);
        });
        
        verify(crawlerService, never()).crawl(any(CrawlRequest.class));
    }
}
