package com.github.khshourov.dsj.writers;

import com.github.khshourov.dsj.db.DictionaryWordDao;
import com.github.khshourov.dsj.models.DictionaryWord;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;

public class WordScrapingWriter implements ItemWriter<DictionaryWord>, InitializingBean {
  private DictionaryWordDao dictionaryWordDao;

  @Override
  public void write(Chunk<? extends DictionaryWord> chunk) throws Exception {
    for (DictionaryWord dictionaryWord : chunk) {
      this.dictionaryWordDao.setLexicalEntry(dictionaryWord);
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
