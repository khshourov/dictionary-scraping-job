package com.github.khshourov.dsj.testlib;

import com.github.khshourov.dsj.db.datasource.EmbeddedDataSourceConfiguration;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {EmbeddedDataSourceConfiguration.class})
public class TruncateDictionaryWord {
  @Autowired protected DataSource dataSource;
  protected JdbcTemplate jdbcTemplate;

  @AfterEach
  void cleanup() {
    // Without RESTART IDENTITY, auto incremented id wouldn't reset to 1
    this.jdbcTemplate.execute("TRUNCATE TABLE dictionary_words RESTART IDENTITY");
  }
}
