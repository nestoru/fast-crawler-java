package com.nestorurquiza.fastcrawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlRequest {
  private String baseUrl;
  private boolean sameDomainOnly;
  private int maxConcurrent;
}
