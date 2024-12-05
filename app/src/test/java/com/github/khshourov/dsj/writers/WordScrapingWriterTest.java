package com.github.khshourov.dsj.writers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.dsj.db.DictionaryWordDao;
import com.github.khshourov.dsj.db.StatusType;
import com.github.khshourov.dsj.models.DictionaryWord;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

class WordScrapingWriterTest {
  private MockDictionaryWordDao dictionaryWordDao;
  private WordScrapingWriter wordScrapingWriter;

  @BeforeEach
  void init() {
    this.dictionaryWordDao = new MockDictionaryWordDao();
    this.wordScrapingWriter = new WordScrapingWriter();
    this.wordScrapingWriter.setDictionaryWordDao(this.dictionaryWordDao);
  }

  @Test
  void scrapedWordsShouldBeSaved() throws Exception {
    DictionaryWord word1 =
        new DictionaryWord(1L, "source", "word1", "lexical-entry1", StatusType.SCRAPED);
    DictionaryWord word2 =
        new DictionaryWord(2L, "source", "word2", "lexical-entry2", StatusType.SCRAPED);

    this.wordScrapingWriter.write(new Chunk<>(List.of(word1, word2)));

    assertEquals(word1, this.dictionaryWordDao.getCalledWith().get(0));
    assertEquals(word2, this.dictionaryWordDao.getCalledWith().get(1));
  }

  private static class MockDictionaryWordDao implements DictionaryWordDao {
    private final List<DictionaryWord> calledWith = new ArrayList<>();

    @Override
    public boolean setLexicalEntry(DictionaryWord dictionaryWord) {
      this.calledWith.add(dictionaryWord);
      return true;
    }

    public List<DictionaryWord> getCalledWith() {
      return this.calledWith;
    }
  }
}
