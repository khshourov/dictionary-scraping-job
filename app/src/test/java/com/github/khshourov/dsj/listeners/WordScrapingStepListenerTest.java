package com.github.khshourov.dsj.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.dsj.db.DictionaryWordDao;
import com.github.khshourov.dsj.db.StatusType;
import com.github.khshourov.dsj.models.DictionaryWord;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WordScrapingStepListenerTest {
  private MockDictionaryWordDao dictionaryWordDao;
  private WordScrapingStepListener listener;
  private DictionaryWord dictionaryWord;

  @BeforeEach
  void init() {
    this.dictionaryWordDao = new MockDictionaryWordDao();
    this.listener = new WordScrapingStepListener();
    this.listener.setDictionaryWordDao(this.dictionaryWordDao);
    this.dictionaryWord = new DictionaryWord("source", "word");
  }

  @Test
  void statusShouldBeMarkedWithNotFoundIfWordCanNotBeFoundAtSourceEnd() {
    this.listener.onSkipInProcess(
        this.dictionaryWord, new NullPointerException("word can not be found"));

    assertEquals(StatusType.NOT_FOUND, this.dictionaryWordDao.getUpdatedStatus());
  }

  @Test
  void statusShouldBeMarkedWithFailedIfThereIsAnErrorInScrapingProcess() {
    this.listener.onSkipInProcess(
        this.dictionaryWord, new TimeoutException("word can not be found"));

    assertEquals(StatusType.FAILED, this.dictionaryWordDao.getUpdatedStatus());
  }

  private static class MockDictionaryWordDao implements DictionaryWordDao {
    private StatusType updatedStatus;

    @Override
    public boolean updateStatus(DictionaryWord dictionaryWord, StatusType updatedStatus) {
      this.updatedStatus = updatedStatus;
      return true;
    }

    public StatusType getUpdatedStatus() {
      return this.updatedStatus;
    }
  }
}
