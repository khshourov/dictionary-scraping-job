package com.github.khshourov.dsj.scraper;

import io.github.khshourov.dictionaryscraper.DefaultDictionaryScraper;
import io.github.khshourov.dictionaryscraper.enums.BaseSource;
import io.github.khshourov.dictionaryscraper.interfaces.DictionaryScraper;
import io.github.khshourov.dictionaryscraper.models.DictionaryWord;
import java.util.Optional;

public class DefaultScraper implements Scraper {
  private final DictionaryScraper dictionaryScraper;

  public DefaultScraper() {
    this.dictionaryScraper = new DefaultDictionaryScraper();
  }

  @Override
  public Optional<String> scrape(String word, String source) {
    DictionaryWord dictionaryWord = this.dictionaryScraper.search(word, BaseSource.valueOf(source));
    if (dictionaryWord == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(dictionaryWord.toJson());
  }
}
