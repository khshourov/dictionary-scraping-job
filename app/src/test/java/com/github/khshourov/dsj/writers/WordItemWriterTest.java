package com.github.khshourov.dsj.writers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.khshourov.dsj.models.DictionaryWord;
import com.github.khshourov.dsj.testlib.TruncateDictionaryWord;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

class WordItemWriterTest extends TruncateDictionaryWord {
  private WordItemWriter wordItemWriter;

  @BeforeEach
  void init() {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.wordItemWriter = new WordItemWriter();
    this.wordItemWriter.setDataSource(dataSource);
  }

  @Test
  void scrapableWordShouldBeInsertedForFurtherProcessing() throws Exception {
    this.wordItemWriter.write(
        new Chunk<>(
            List.of(new DictionaryWord("source", "word1"), new DictionaryWord("source", "word2"))));

    assertEquals(
        1,
        JdbcTestUtils.countRowsInTableWhere(
            this.jdbcTemplate, "dictionary_words", "word = 'word1'"));
    assertEquals(
        1,
        JdbcTestUtils.countRowsInTableWhere(
            this.jdbcTemplate, "dictionary_words", "word = 'word2'"));
    assertEquals(
        2,
        JdbcTestUtils.countRowsInTableWhere(
            this.jdbcTemplate, "dictionary_words", "status = 'CREATED'"));
  }
}
