package com.github.khshourov.dsj.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

class JdbcDictionaryWordDaoTest extends TruncateDictionaryWord {
  private JdbcDictionaryWordDao dictionaryWordDao;
  private DictionaryWord dictionaryWord;

  @BeforeEach
  void init() {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.dictionaryWordDao = new JdbcDictionaryWordDao();
    this.dictionaryWordDao.setDataSource(dataSource);

    this.jdbcTemplate.update("INSERT INTO dictionary_words(source, word) VALUES('source', 'word')");
    this.dictionaryWord = new DictionaryWord(1L, "source", "word", null, StatusType.CREATED);
  }

  @ParameterizedTest
  @MethodSource("validInitialStatus")
  void wordCanBeProcessedIfStatusIsInValidState(String validInitialStatus) {
    this.jdbcTemplate.update(
        "UPDATE dictionary_words SET status = ?, updated_at = NULL WHERE id = 1",
        validInitialStatus);

    boolean updated = this.dictionaryWordDao.makeEntryAsProcessing(this.dictionaryWord);

    assertTrue(updated);
    String actualStatus =
        this.jdbcTemplate.queryForObject(
            "SELECT status FROM dictionary_words WHERE id = 1", String.class);
    assertEquals(StatusType.SCRAPING.name(), actualStatus);

    String updatedAt =
        this.jdbcTemplate.queryForObject(
            "SELECT updated_at FROM dictionary_words WHERE id = 1", String.class);
    assertNotNull(updatedAt);
  }

  @ParameterizedTest
  @MethodSource("invalidInitialStatus")
  void wordCanNotBeProcessedIfItIsNotInValidState(String invalidInitialStatus) {
    this.jdbcTemplate.update(
        "UPDATE dictionary_words SET status = ?, updated_at = NULL WHERE id = 1",
        invalidInitialStatus);

    boolean updated = this.dictionaryWordDao.makeEntryAsProcessing(this.dictionaryWord);

    assertFalse(updated);
    String actualStatus =
        this.jdbcTemplate.queryForObject(
            "SELECT status FROM dictionary_words WHERE id = 1", String.class);
    assertEquals(invalidInitialStatus, actualStatus);

    String updatedAt =
        this.jdbcTemplate.queryForObject(
            "SELECT updated_at FROM dictionary_words WHERE id = 1", String.class);
    assertNull(updatedAt);
  }

  @Test
  void lexicalEntryCanBeSetForScrapingState() {
    this.jdbcTemplate.update(
        "UPDATE dictionary_words SET status = 'SCRAPING', updated_at = NULL WHERE id = 1");

    boolean updated = this.dictionaryWordDao.setLexicalEntry(this.dictionaryWord);

    assertTrue(updated);
    String actualStatus =
        this.jdbcTemplate.queryForObject(
            "SELECT status FROM dictionary_words WHERE id = 1", String.class);
    assertEquals(StatusType.SCRAPED.name(), actualStatus);

    String updatedAt =
        this.jdbcTemplate.queryForObject(
            "SELECT updated_at FROM dictionary_words WHERE id = 1", String.class);
    assertNotNull(updatedAt);
  }

  @Test
  void lexicalEntryCanNotBeSetForInvalidState() {
    this.jdbcTemplate.update(
        "UPDATE dictionary_words SET status = 'FAILED', updated_at = NULL WHERE id = 1");

    boolean updated = this.dictionaryWordDao.setLexicalEntry(this.dictionaryWord);

    assertFalse(updated);
    String actualStatus =
        this.jdbcTemplate.queryForObject(
            "SELECT status FROM dictionary_words WHERE id = 1", String.class);
    assertEquals(StatusType.FAILED.name(), actualStatus);

    String updatedAt =
        this.jdbcTemplate.queryForObject(
            "SELECT updated_at FROM dictionary_words WHERE id = 1", String.class);
    assertNull(updatedAt);
  }

  @Test
  void statusCanBeSetForValidId() {
    this.jdbcTemplate.update(
        "UPDATE dictionary_words SET status = 'CREATED', updated_at = NULL WHERE id = 1");

    boolean updated = this.dictionaryWordDao.updateStatus(this.dictionaryWord, StatusType.FAILED);

    assertTrue(updated);
    String actualStatus =
        this.jdbcTemplate.queryForObject(
            "SELECT status FROM dictionary_words WHERE id = 1", String.class);
    assertEquals(StatusType.FAILED.name(), actualStatus);

    String updatedAt =
        this.jdbcTemplate.queryForObject(
            "SELECT updated_at FROM dictionary_words WHERE id = 1", String.class);
    assertNotNull(updatedAt);
  }

  @Test
  void statusCanNotBeSetForInvalidId() {
    this.jdbcTemplate.update(
        "UPDATE dictionary_words SET status = 'CREATED', updated_at = NULL WHERE id = 1");

    boolean updated =
        this.dictionaryWordDao.updateStatus(
            new DictionaryWord(2L, "source", "word", "lexical_entry", StatusType.CREATED),
            StatusType.FAILED);

    assertFalse(updated);
    String actualStatus =
        this.jdbcTemplate.queryForObject(
            "SELECT status FROM dictionary_words WHERE id = 1", String.class);
    assertEquals(StatusType.CREATED.name(), actualStatus);

    String updatedAt =
        this.jdbcTemplate.queryForObject(
            "SELECT updated_at FROM dictionary_words WHERE id = 1", String.class);
    assertNull(updatedAt);
  }

  static Stream<Arguments> validInitialStatus() {
    return Stream.of(arguments(StatusType.CREATED.name()), arguments(StatusType.FAILED.name()));
  }

  static Stream<Arguments> invalidInitialStatus() {
    return Stream.of(
        arguments(StatusType.SCRAPED.name()),
        arguments(StatusType.SCRAPING.name()),
        arguments(StatusType.NOT_FOUND.name()));
  }
}
