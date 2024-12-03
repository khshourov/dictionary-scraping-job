package com.github.khshourov.dsj.listeners;

import com.github.khshourov.dsj.db.DictionaryWordDao;
import com.github.khshourov.dsj.db.StatusType;
import com.github.khshourov.dsj.models.DictionaryWord;
import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.InitializingBean;

public class WordScrapingStepListener
    implements SkipListener<DictionaryWord, DictionaryWord>, InitializingBean {
  private DictionaryWordDao dictionaryWordDao;

  @Override
  public void onSkipInProcess(DictionaryWord item, Throwable t) {
    if (t instanceof NullPointerException) {
      this.dictionaryWordDao.updateStatus(item, StatusType.NOT_FOUND);
    } else {
      this.dictionaryWordDao.updateStatus(item, StatusType.FAILED);
    }
  }

  public void setDictionaryWordDao(DictionaryWordDao dictionaryWordDao) {
    this.dictionaryWordDao = dictionaryWordDao;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.dictionaryWordDao == null) {
      throw new IllegalStateException("dictionaryWordDao needs to be set");
    }
  }
}
