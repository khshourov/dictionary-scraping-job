package com.github.khshourov.dsj.scraper;

import java.util.Optional;

public interface Scraper {
  Optional<String> scrape(String word, String source);
}
