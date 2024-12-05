package com.github.khshourov.dsj.processors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.github.khshourov.dsj.models.DictionaryWord;
import com.github.khshourov.dsj.testlib.TruncateDictionaryWord;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.JdbcTemplate;

class WordItemProcessorTest extends TruncateDictionaryWord {
  private WordItemProcessor processor;

  @BeforeEach
  void init() {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.processor = new WordItemProcessor();
    this.processor.setDataSource(dataSource);

    this.jdbcTemplate.execute(
        "INSERT INTO dictionary_words(source, word) VALUES('source', 'word')");
  }

  @Test
  void duplicatePairOfSourceAndWordShouldThrowException() {
    DictionaryWord dictionaryWord = new DictionaryWord("source", "word");

    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> this.processor.process(dictionaryWord));
    assertEquals("(source, word) is already exists", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("validDictionaryWords")
  void duplicatePairOfSourceAndWordShouldThrowException(DictionaryWord validDictionaryWord) {
    assertDoesNotThrow(() -> this.processor.process(validDictionaryWord));
  }

  static Stream<Arguments> validDictionaryWords() {
    return Stream.of(
        arguments(new DictionaryWord("source", "word1")),
        arguments(new DictionaryWord("source1", "word")),
        arguments(new DictionaryWord("source1", "word1")));
  }
}
