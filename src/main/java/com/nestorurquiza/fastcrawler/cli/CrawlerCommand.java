package com.nestorurquiza.fastcrawler.cli;

import com.nestorurquiza.fastcrawler.model.CrawlRequest;
import com.nestorurquiza.fastcrawler.model.CrawlResult;
import com.nestorurquiza.fastcrawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerCommand implements CommandLineRunner {
  private final CrawlerService crawlerService;

  @Override
  public void run(String... args) {
    if (args.length < 1) {
      System.out.println(
          "Usage: java -jar fast-crawler.jar <base-url> [--same-domain-only] [--max-concurrent <num>] [--error-level <level>]");
      throw new IllegalArgumentException("Missing required base URL argument");
    }

    try {
      CrawlRequest request = parseArgs(args);
      log.debug("Starting crawl with request: {}", request);

      CrawlResult result = crawlerService.crawl(request);
      log.debug("Crawl completed");

      System.out.println("\nCrawl completed in " + result.getDuration().toSeconds() + " seconds");
      System.out.println("Visited " + result.getTotalPages() + " pages");
      System.out.println("\nCrawled URLs:");
      result.getVisitedUrls().stream().sorted().forEach(System.out::println);
    } catch (Exception e) {
      log.error("Error during crawl: ", e);
      System.err.println("Error: " + e.getMessage());
      throw e;
    }
  }

  private CrawlRequest parseArgs(String[] args) {
    String baseUrl = args[0];
    boolean sameDomainOnly = false;
    int maxConcurrent = 5;

    for (int i = 1; i < args.length; i++) {
      switch (args[i]) {
        case "--same-domain-only":
          sameDomainOnly = true;
          break;
        case "--max-concurrent":
          if (i + 1 < args.length) {
            maxConcurrent = Integer.parseInt(args[++i]);
          }
          break;
      }
    }

    return CrawlRequest.builder()
        .baseUrl(baseUrl)
        .sameDomainOnly(sameDomainOnly)
        .maxConcurrent(maxConcurrent)
        .build();
  }
}
