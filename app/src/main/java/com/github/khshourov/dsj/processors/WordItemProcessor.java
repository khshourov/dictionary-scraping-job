package com.github.khshourov.dsj.processors;

import com.github.khshourov.dsj.models.Word;
import javax.sql.DataSource;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

public class WordItemProcessor implements ItemProcessor<Word, Word>, InitializingBean {
  private static final String CHECK_DUPLICATE =
      "SELECT COUNT(*) FROM dictionary_words WHERE source = ? AND word = ?";

  private JdbcTemplate jdbcTemplate;

  @Override
  public Word process(Word item) throws Exception {
    int exists =
        this.jdbcTemplate.queryForObject(
            CHECK_DUPLICATE, new String[] {item.source(), item.word()}, Integer.class);
    if (exists == 1) {
      throw new IllegalArgumentException(
          String.format("(%s, %s) is already exists", item.source(), item.word()));
    }

    return item;
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
