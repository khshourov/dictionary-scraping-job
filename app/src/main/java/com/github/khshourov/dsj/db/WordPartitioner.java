package com.github.khshourov.dsj.db;

import com.github.khshourov.dsj.lib.Utils;
import java.util.HashMap;
import java.util.List;
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
            .query("SELECT MIN(id) FROM dictionary_words", (rs, rowNumber) -> rs.getInt(1))
            .getFirst();
    int max =
        this.jdbcTemplate
            .query("SELECT MAX(id) FROM dictionary_words", (rs, rowNumber) -> rs.getInt(1))
            .getFirst();

    Map<String, ExecutionContext> partitions = new HashMap<>();

    List<int[]> ranges = Utils.createPartitions(min, max, gridSize);
    for (int i = 0; i < gridSize; i++) {
      ExecutionContext executionContext = new ExecutionContext();
      executionContext.put("minId", ranges.get(i)[0]);
      executionContext.put("maxId", ranges.get(i)[1]);

      partitions.put(String.format("partition%d", i), executionContext);
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
