package com.github.khshourov.dsj.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.khshourov.dsj.testlib.TruncateDictionaryWord;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

class WordPartitionerTest extends TruncateDictionaryWord {
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
}
