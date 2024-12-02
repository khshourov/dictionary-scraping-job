package com.github.khshourov.dsj.db.datasource;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
@Profile("test")
public class EmbeddedDataSourceConfiguration {
  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("/org/springframework/batch/core/schema-drop-hsqldb.sql")
        .addScript("/org/springframework/batch/core/schema-hsqldb.sql")
        .addScript("/com/github/khshourov/dsj/schema/starter-schema.sql")
        .generateUniqueName(true)
        .build();
  }

  @Bean
  public JdbcTransactionManager transactionManager(DataSource dataSource) {
    return new JdbcTransactionManager(dataSource);
  }
}
