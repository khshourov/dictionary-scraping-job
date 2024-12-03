package com.github.khshourov.dsj.db;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

public class WordPartitioner implements Partitioner, InitializingBean {
  private JdbcTemplate jdbcTemplate;

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    int min =
        this.jdbcTemplate
            .query("SELECT MIN(id) FROM dictionary_words", (rs, rowNumber) -> rs.getInt(0))
            .getFirst();
    int max =
        this.jdbcTemplate
            .query("SELECT MAX(id) FROM dictionary_words", (rs, rowNumber) -> rs.getInt(0))
            .getFirst();
    int targetSize = (max - min) / gridSize + 1;

    Map<String, ExecutionContext> partitions = new HashMap<>();

    int partitionNumber = 0;
    int start = min;
    int end = start + targetSize - 1;
    while (start <= max) {
      ExecutionContext executionContext = new ExecutionContext();
      end = Math.min(end, max);
      executionContext.put("minId", start);
      executionContext.put("maxId", end);

      partitions.put(String.format("partition%d", partitionNumber), executionContext);

      start = start + targetSize;
      end = end + targetSize;
      partitionNumber = partitionNumber + 1;
    }

    return partitions;
  }

  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.jdbcTemplate == null) {
      throw new IllegalStateException("dataSource needs to be set");
    }
  }
}
