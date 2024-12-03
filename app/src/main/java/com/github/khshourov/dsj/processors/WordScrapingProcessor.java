package com.github.khshourov.dsj.processors;

import com.github.khshourov.dsj.db.DictionaryWordDao;
import com.github.khshourov.dsj.db.StatusType;
import com.github.khshourov.dsj.models.DictionaryWord;
import com.github.khshourov.dsj.scraper.Scraper;
import org.springframework.batch.item.ItemProcessor;

public class WordScrapingProcessor implements ItemProcessor<DictionaryWord, DictionaryWord> {
  private Scraper scraper;
  private DictionaryWordDao dictionaryWordDao;

  @Override
  public DictionaryWord process(DictionaryWord item) throws Exception {
    boolean updated = this.dictionaryWordDao.makeEntryAsProcessing(item);
    if (!updated) {
      throw new IllegalStateException(
          String.format("(id: %d, word: %s) can not be processed", item.id(), item.word()));
    }

    try {
      Object response = scraper.scrape(item.word(), item.source());
      if (response == null) {
        throw new NullPointerException(
            String.format("%s not found in %s", item.word(), item.source()));
      }
      return new DictionaryWord(
          item.id(), item.source(), item.word(), (String) response, StatusType.SCRAPED);
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  public void setDictionaryWordDao(DictionaryWordDao dictionaryWordDao) {
    this.dictionaryWordDao = dictionaryWordDao;
  }
}
