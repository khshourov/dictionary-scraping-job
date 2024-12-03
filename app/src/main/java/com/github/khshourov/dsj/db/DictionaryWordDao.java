package com.github.khshourov.dsj.db;

import com.github.khshourov.dsj.models.DictionaryWord;

public interface DictionaryWordDao {
  default boolean makeEntryAsProcessing(DictionaryWord dictionaryWord) {
    return false;
  }

  default void setLexicalEntry(DictionaryWord dictionaryWord) {}

  default boolean updateStatus(DictionaryWord dictionaryWord, StatusType updatedStatus) {
    return false;
  }
}
