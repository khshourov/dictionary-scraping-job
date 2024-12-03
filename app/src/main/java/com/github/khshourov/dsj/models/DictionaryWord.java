package com.github.khshourov.dsj.models;

import com.github.khshourov.dsj.db.StatusType;

public record DictionaryWord(
    Long id, String source, String word, String lexicalEntry, StatusType status) {
  public DictionaryWord(String source, String word) {
    this(0L, source, word, null, StatusType.CREATED);
  }
}
