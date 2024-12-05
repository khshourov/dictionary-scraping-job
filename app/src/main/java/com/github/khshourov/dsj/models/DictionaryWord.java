package com.github.khshourov.dsj.models;

import com.github.khshourov.dsj.db.StatusType;
import java.util.Objects;

public record DictionaryWord(
    Long id, String source, String word, String lexicalEntry, StatusType status) {
  public DictionaryWord(String source, String word) {
    this(0L, source, word, null, StatusType.CREATED);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DictionaryWord other)) {
      return false;
    }

    return Objects.equals(this.id, other.id)
        && Objects.equals(this.source, other.source)
        && Objects.equals(this.word, other.word);
  }
}
