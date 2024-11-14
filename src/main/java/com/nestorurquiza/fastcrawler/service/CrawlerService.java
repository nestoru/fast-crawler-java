package com.nestorurquiza.fastcrawler.service;

import com.nestorurquiza.fastcrawler.model.CrawlRequest;
import com.nestorurquiza.fastcrawler.model.CrawlResult;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {
  private final ThreadPoolTaskExecutor taskExecutor;
  private final UrlValidator urlValidator = new UrlValidator(new String[] {"http", "https"});

  public CrawlResult crawl(CrawlRequest request) {
    validateRequest(request);

    Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
    BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();
    urlQueue.add(request.getBaseUrl());

    Instant startTime = Instant.now();
    boolean isProcessing = true;

    while (isProcessing && !Thread.currentThread().isInterrupted()) {
      try {
        // Wait for all current batch processing to complete
        CountDownLatch batchLatch = processBatch(urlQueue, visitedUrls, request);
        batchLatch.await(30, TimeUnit.SECONDS);

        // Check if we should continue processing
        isProcessing = !urlQueue.isEmpty() || taskExecutor.getActiveCount() > 0;

        if (!isProcessing) {
          // Double check after a small delay to ensure no new URLs were added
          Thread.sleep(100);
          isProcessing = !urlQueue.isEmpty() || taskExecutor.getActiveCount() > 0;
        }

        log.debug(
            "Queue size: {}, Active threads: {}, URLs visited: {}",
            urlQueue.size(),
            taskExecutor.getActiveCount(),
            visitedUrls.size());

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("Crawler interrupted", e);
        break;
      }
    }

    Duration duration = Duration.between(startTime, Instant.now());
    return CrawlResult.builder()
        .visitedUrls(visitedUrls)
        .duration(duration)
        .totalPages(visitedUrls.size())
        .build();
  }

  private CountDownLatch processBatch(
      BlockingQueue<String> urlQueue, Set<String> visitedUrls, CrawlRequest request) {
    int batchSize = Math.min(request.getMaxConcurrent(), urlQueue.size());
    if (batchSize == 0) return new CountDownLatch(0);

    CountDownLatch batchLatch = new CountDownLatch(batchSize);

    for (int i = 0; i < batchSize; i++) {
      String url = urlQueue.poll();
      if (url != null) {
        taskExecutor.submit(
            () -> {
              try {
                processUrl(url, urlQueue, visitedUrls, request);
              } finally {
                batchLatch.countDown();
              }
            });
      } else {
        batchLatch.countDown();
      }
    }

    return batchLatch;
  }

  private void processUrl(
      String url, BlockingQueue<String> urlQueue, Set<String> visitedUrls, CrawlRequest request) {
    if (!visitedUrls.add(url)) {
      return;
    }

    try {
      log.debug("Processing URL: {}", url);
      Document doc =
          Jsoup.connect(url)
              .timeout(10000)
              .userAgent("Mozilla/5.0 (compatible; FastCrawler/1.0)")
              .get();

      Set<String> newUrls =
          doc.select("a[href]").stream()
              .map(element -> element.attr("abs:href"))
              .filter(href -> !href.isEmpty())
              .filter(href -> urlValidator.isValid(href))
              .filter(href -> !visitedUrls.contains(href))
              .filter(
                  href -> !request.isSameDomainOnly() || isSameDomain(href, request.getBaseUrl()))
              .collect(Collectors.toSet());

      log.debug("Found {} new URLs from {}", newUrls.size(), url);
      newUrls.forEach(urlQueue::offer);

    } catch (IOException e) {
      log.error("Error processing URL: {}", url, e);
    }
  }

  private boolean isSameDomain(String url1, String url2) {
    try {
      String domain1 = new URI(url1).getHost();
      String domain2 = new URI(url2).getHost();
      return domain1 != null
          && domain2 != null
          && (domain1.equals(domain2)
              || domain1.endsWith("." + domain2)
              || domain2.endsWith("." + domain1));
    } catch (URISyntaxException e) {
      return false;
    }
  }

  private void validateRequest(CrawlRequest request) {
    if (!urlValidator.isValid(request.getBaseUrl())) {
      throw new IllegalArgumentException("Invalid base URL provided");
    }
    if (request.getMaxConcurrent() < 1) {
      throw new IllegalArgumentException("Max concurrent processes must be greater than 0");
    }
  }
}
