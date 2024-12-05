package com.github.khshourov.dsj.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.khshourov.dsj.db.DictionaryWordDao;
import com.github.khshourov.dsj.models.DictionaryWord;
import com.github.khshourov.dsj.scraper.Scraper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WordScrapingProcessorTest {
  private MockScraper scraper;
  private MockDictionaryWordDao dictionaryWordDao;
  private WordScrapingProcessor processor;
  private DictionaryWord dictionaryWord;

  @BeforeEach
  void init() {
    this.scraper = new MockScraper();
    this.dictionaryWordDao = new MockDictionaryWordDao();
    this.processor = new WordScrapingProcessor();
    this.processor.setDictionaryWordDao(this.dictionaryWordDao);
    this.processor.setScraper(this.scraper);
    this.dictionaryWord = new DictionaryWord("source", "word");
  }

  @Test
  void exceptionShouldBeThrownForUnprocessableEntry() {
    this.dictionaryWordDao.setProcessable(false);

    Exception exception =
        assertThrows(
            IllegalStateException.class, () -> this.processor.process(this.dictionaryWord));
    assertEquals("(id: 0, word: word) can not be processed", exception.getMessage());
  }

  @Test
  void nullPointerExceptionShouldBeThrownIfWordCanNotBeFound() {
    this.dictionaryWordDao.setProcessable(true);
    this.scraper.setResponse(null);

    Exception exception =
        assertThrows(NullPointerException.class, () -> this.processor.process(this.dictionaryWord));
    assertEquals("word not found in source", exception.getMessage());
  }

  @Test
  void exceptionShouldBeThrownIfWordCanNotBeScraped() {
    this.dictionaryWordDao.setProcessable(true);
    this.scraper.setThrowable(new RuntimeException("exception"));

    Exception exception =
        assertThrows(
            IllegalStateException.class, () -> this.processor.process(this.dictionaryWord));
    assertEquals("exception", exception.getMessage());
  }

  @Test
  void successfulScrapingShouldReturnValidDictionaryWord() throws Exception {
    this.dictionaryWordDao.setProcessable(true);
    this.scraper.setResponse("response");

    DictionaryWord scrapedWord = this.processor.process(this.dictionaryWord);

    assertNotNull(scrapedWord);
    assertEquals("response", scrapedWord.lexicalEntry());
    assertEquals("word", this.scraper.getCalledWith().get(0));
    assertEquals("source", this.scraper.getCalledWith().get(1));
  }

  private static class MockDictionaryWordDao implements DictionaryWordDao {
    private boolean processable;

    @Override
    public boolean makeEntryAsProcessing(DictionaryWord dictionaryWord) {
      return this.processable;
    }

    public void setProcessable(boolean processable) {
      this.processable = processable;
    }
  }

  private static class MockScraper implements Scraper {
    private Object response;
    private Throwable throwable;
    private List<String> calledWith = new ArrayList<>();

    @Override
    public Object scrape(String word, String source) {
      if (this.throwable != null) {
        throw new RuntimeException(this.throwable.getMessage());
      }

      this.calledWith.add(word);
      this.calledWith.add(source);

      return this.response;
    }

    public void setResponse(Object response) {
      this.response = response;
    }

    public void setThrowable(Throwable throwable) {
      this.throwable = throwable;
    }

    public List<String> getCalledWith() {
      return this.calledWith;
    }
  }
}
