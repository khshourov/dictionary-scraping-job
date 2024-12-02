package com.github.khshourov.dsj.writers;

import com.github.khshourov.dsj.models.Word;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ListIterator;
import javax.sql.DataSource;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

public class WordItemWriter implements ItemWriter<Word>, InitializingBean {
  private static final String INSERT_WORD =
      "INSERT INTO dictionary_words(source, word) VALUES(?, ?)";

  private JdbcTemplate jdbcTemplate;

  @Override
  public void write(Chunk<? extends Word> chunk) throws Exception {
    final ListIterator<? extends Word> itemIterator = chunk.getItems().listIterator();

    this.jdbcTemplate.batchUpdate(
        INSERT_WORD,
        new BatchPreparedStatementSetter() {
          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            Word entry = itemIterator.next();

            ps.setString(1, entry.source());
            ps.setString(2, entry.word());
          }

          @Override
          public int getBatchSize() {
            return chunk.size();
          }
        });
  }

  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.jdbcTemplate == null) {
      throw new IllegalStateException("dataSource has not been set");
    }
  }
}
