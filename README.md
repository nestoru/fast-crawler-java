# Fast Crawler

A Java based high-performance web crawler with support for concurrent processing.

## Preconditions
Running all this in OSX here
- Java
```
brew install openjdk@17
```
- Maven
```
brew install maven
```

## Installation

```bash
# Install with Maven
mvn clean package
```

## Usage

```bash
# Run the crawler
java -jar target/fast-crawler-1.0.0.jar https://example.com --same-domain-only --max-concurrent 5 --error-level DEBUG
```

## Features

- Concurrent web crawling
- Domain and subdomain filtering
- Configurable concurrency level
- Comprehensive logging system
- Performance metrics tracking

## Development

```bash
# Run tests
mvn test

# Format code
mvn spotless:apply

# Lint code
mvn checkstyle:check
```

## Sharing all relevant code. Use the following find (fd) command. Pipe the result to the pbcopy command if you want it in the clipboard
```
fd -H -t f --exclude '.git' --exclude '.gitignore' --exclude 'target'  -0 | xargs -0 -I {} sh -c 'echo "File: {}"; cat {}'
```

## License

MIT
