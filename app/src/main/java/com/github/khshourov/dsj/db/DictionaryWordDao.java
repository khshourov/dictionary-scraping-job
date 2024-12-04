package com.github.khshourov.dsj.db;

import com.github.khshourov.dsj.models.DictionaryWord;

public interface DictionaryWordDao {
  default boolean makeEntryAsProcessing(DictionaryWord dictionaryWord) {
    return false;
  }

  default boolean setLexicalEntry(DictionaryWord dictionaryWord) {
    return false;
  }

  default boolean updateStatus(DictionaryWord dictionaryWord, StatusType updatedStatus) {
    return false;
  }
}
