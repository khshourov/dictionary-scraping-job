package com.github.khshourov.dsj.db;

import com.github.khshourov.dsj.models.DictionaryWord;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcDictionaryWordDao implements DictionaryWordDao {
  private static final String UPDATE_STATUS_TO_PROCESSING =
      "UPDATE dictionary_words SET status = ? WHERE id = ? AND (status = ? OR status = ?)";
  private static final String UPDATE_LEXICAL_ENTRY =
      "UPDATE dictionary_words SET lexical_entry = ?, status = ? WHERE id = ? AND status = ?";
  private static final String UPDATE_STATUS = "UPDATE dictionary_words SET status = ? WHERE id = ?";

  private JdbcTemplate jdbcTemplate;

  @Override
  public boolean makeEntryAsProcessing(DictionaryWord dictionaryWord) {
    int updatedRowCount =
        this.jdbcTemplate.update(
            UPDATE_STATUS_TO_PROCESSING,
            StatusType.SCRAPING.name(),
            dictionaryWord.id(),
            StatusType.CREATED.name(),
            StatusType.FAILED.name());
    return updatedRowCount == 1;
  }

  @Override
  public void setLexicalEntry(DictionaryWord dictionaryWord) {
    this.jdbcTemplate.update(
        UPDATE_LEXICAL_ENTRY,
        dictionaryWord.lexicalEntry(),
        dictionaryWord.status().name(),
        dictionaryWord.id(),
        StatusType.SCRAPING.name());
  }

  @Override
  public boolean updateStatus(DictionaryWord dictionaryWord, StatusType updatedStatus) {
    int updatedRowCount =
        this.jdbcTemplate.update(UPDATE_STATUS, updatedStatus.name(), dictionaryWord.id());
    return updatedRowCount == 1;
  }

  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }
}
