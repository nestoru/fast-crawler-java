package com.nestorurquiza.fastcrawler.model;

import java.time.Duration;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlResult {
  private Set<String> visitedUrls;
  private Duration duration;
  private int totalPages;
}
