package com.nestorurquiza.fastcrawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

@Slf4j
@SpringBootApplication
public class FastCrawlerApplication {
  public static void main(String[] args) {
    // Parse logging level before starting the context
    LogLevel logLevel = parseLogLevel(args);

    // Set logging.level.com.nestorurquiza.fastcrawler in environment
    ConfigurableEnvironment environment = new StandardEnvironment();
    environment
        .getSystemProperties()
        .put("logging.level.com.nestorurquiza.fastcrawler", logLevel.name());

    ConfigurableApplicationContext context =
        new SpringApplicationBuilder(FastCrawlerApplication.class)
            .web(WebApplicationType.NONE)
            .bannerMode(Banner.Mode.OFF)
            .environment(environment)
            .run(args);

    context.close();
    System.exit(0);
  }

  private static LogLevel parseLogLevel(String[] args) {
    for (int i = 0; i < args.length - 1; i++) {
      if ("--error-level".equals(args[i]) && i + 1 < args.length) {
        try {
          return LogLevel.valueOf(args[i + 1].toUpperCase());
        } catch (IllegalArgumentException e) {
          // Default to ERROR if invalid level provided
          return LogLevel.ERROR;
        }
      }
    }
    return LogLevel.ERROR; // Default level
  }
}
