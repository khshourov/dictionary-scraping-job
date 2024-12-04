package com.github.khshourov.dsj.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.dsj.db.datasource.EmbeddedDataSourceConfiguration;
import java.util.Map;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {EmbeddedDataSourceConfiguration.class})
class WordPartitionerTest {
  @Autowired private DataSource dataSource;
  private JdbcTemplate jdbcTemplate;
  private WordPartitioner wordPartitioner;

  @BeforeEach
  void init() {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.wordPartitioner = new WordPartitioner();
    this.wordPartitioner.setDataSource(dataSource);
  }

  @Test
  void partitionerShouldPartitionerWithValidRangeOfId() {
    this.jdbcTemplate.execute(
        "INSERT INTO dictionary_words(id, source, word) VALUES(1, 'source', 'word1')");
    this.jdbcTemplate.execute(
        "INSERT INTO dictionary_words(id, source, word) VALUES(100, 'source', 'word2')");

    int[][] expected =
        new int[][] {
          {1, 20},
          {21, 40},
          {41, 60},
          {61, 80},
          {81, 100}
        };

    Map<String, ExecutionContext> partitions = this.wordPartitioner.partition(5);
    IntStream.range(0, 5)
        .forEachOrdered(
            i -> {
              assertTrue(partitions.containsKey("partition" + i));

              ExecutionContext context = partitions.get("partition" + i);
              assertEquals(expected[i][0], context.get("minId"));
              assertEquals(expected[i][1], context.get("maxId"));
            });
  }

  @Test
  void partitionerShouldWorkWithEmptyTable() {
    int[][] expected =
        new int[][] {
          {0, 0},
          {0, 0}
        };

    Map<String, ExecutionContext> partitions = this.wordPartitioner.partition(2);
    IntStream.range(0, 2)
        .forEachOrdered(
            i -> {
              assertTrue(partitions.containsKey("partition" + i));

              ExecutionContext context = partitions.get("partition" + i);
              assertEquals(expected[i][0], context.get("minId"));
              assertEquals(expected[i][1], context.get("maxId"));
            });
  }

  @AfterEach
  void cleanup() {
    // Without RESTART IDENTITY, auto incremented id wouldn't reset to 1
    this.jdbcTemplate.execute("TRUNCATE TABLE dictionary_words RESTART IDENTITY");
  }
}
