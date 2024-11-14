package com.nestorurquiza.fastcrawler.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nestorurquiza.fastcrawler.model.CrawlRequest;
import com.nestorurquiza.fastcrawler.model.CrawlResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@ExtendWith(MockitoExtension.class)
class CrawlerServiceTest {

  @Mock private ThreadPoolTaskExecutor taskExecutor;

  private CrawlerService crawlerService;

  @BeforeEach
  void setUp() {
    crawlerService = new CrawlerService(taskExecutor);
  }

  @Test
  void shouldValidateRequestUrl() {
    CrawlRequest invalidRequest =
        CrawlRequest.builder().baseUrl("invalid-url").maxConcurrent(1).build();

    assertThrows(IllegalArgumentException.class, () -> crawlerService.crawl(invalidRequest));
  }

  @Test
  void shouldValidateMaxConcurrent() {
    CrawlRequest invalidRequest =
        CrawlRequest.builder().baseUrl("https://example.com").maxConcurrent(0).build();

    assertThrows(IllegalArgumentException.class, () -> crawlerService.crawl(invalidRequest));
  }

  @Test
  void shouldHandleValidRequest() {
    CrawlRequest validRequest =
        CrawlRequest.builder().baseUrl("https://example.com").maxConcurrent(1).build();

    when(taskExecutor.submit(any(Runnable.class))).thenReturn(null);

    CrawlResult result = crawlerService.crawl(validRequest);

    assertNotNull(result);
    assertNotNull(result.getVisitedUrls());
    assertNotNull(result.getDuration());
    assertTrue(result.getTotalPages() >= 0);
  }
}
