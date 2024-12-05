package com.github.khshourov.dsj.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.dsj.models.DictionaryWord;
import com.github.khshourov.dsj.testlib.TruncateDictionaryWord;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class DictionaryWordRowMapperTest extends TruncateDictionaryWord {
  @BeforeEach
  void init() {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Test
  void mapperShouldMappedResultSetToDictionaryWord() {
    String insertValidDictionaryWord =
        "INSERT INTO dictionary_words(source, word, lexical_entry, status) "
            + "VALUES('source', 'word', 'lexical_entry', 'CREATED')";
    String fetchDictionaryWord = "SELECT * FROM dictionary_words WHERE word = 'word'";

    this.jdbcTemplate.update(insertValidDictionaryWord);
    DictionaryWordRowMapper mapper = new DictionaryWordRowMapper();

    List<DictionaryWord> results =
        this.jdbcTemplate.query(fetchDictionaryWord, new Object[] {}, new int[] {}, mapper);

    DictionaryWord dictionaryWord = results.getFirst();
    assertEquals("source", dictionaryWord.source());
    assertEquals("word", dictionaryWord.word());
    assertEquals("lexical_entry", dictionaryWord.lexicalEntry());
    assertEquals(StatusType.CREATED, dictionaryWord.status());
  }
}
