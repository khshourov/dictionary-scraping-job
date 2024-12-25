package com.github.khshourov.dsj.jobs;

import com.github.khshourov.dsj.db.DictionaryWordRowMapper;
import com.github.khshourov.dsj.db.JdbcDictionaryWordDao;
import com.github.khshourov.dsj.db.StatusType;
import com.github.khshourov.dsj.db.WordPartitioner;
import com.github.khshourov.dsj.db.datasource.EmbeddedDataSourceConfiguration;
import com.github.khshourov.dsj.db.datasource.PersistentDataSourceConfiguration;
import com.github.khshourov.dsj.listeners.WordScrapingStepListener;
import com.github.khshourov.dsj.models.DictionaryWord;
import com.github.khshourov.dsj.processors.WordScrapingProcessor;
import com.github.khshourov.dsj.scraper.DefaultScraper;
import com.github.khshourov.dsj.scraper.Scraper;
import com.github.khshourov.dsj.writers.WordScrapingWriter;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
@EnableBatchProcessing
@Import({PersistentDataSourceConfiguration.class, EmbeddedDataSourceConfiguration.class})
public class WordsScrapingJob {
  @Bean
  Job scrapingJob(JobRepository jobRepository, Step partitionStep) {
    return new JobBuilder("wordsScrapingJob", jobRepository).start(partitionStep).build();
  }

  @Bean
  Step partitionStep(
      JobRepository jobRepository, Partitioner wordPartitioner, Step wordScrapingStep) {
    return new StepBuilder("partitionStep", jobRepository)
        .partitioner("wordPartitioner", wordPartitioner)
        .step(wordScrapingStep)
        .taskExecutor(new SimpleAsyncTaskExecutor())
        .gridSize(8)
        .build();
  }

  @Bean
  Partitioner wordPartitioner(DataSource dataSource) throws Exception {
    WordPartitioner partitioner = new WordPartitioner();
    partitioner.setDataSource(dataSource);
    partitioner.afterPropertiesSet();
    return partitioner;
  }

  @Bean
  Step wordScrapingStep(
      JobRepository jobRepository,
      JdbcTransactionManager transactionManager,
      JdbcPagingItemReader<DictionaryWord> wordJdbcPagingItemReader,
      WordScrapingProcessor wordScrapingProcessor,
      WordScrapingWriter wordScrapingWriter,
      WordScrapingStepListener wordScrapingStepListener) {
    return new StepBuilder("wordScrapingStep", jobRepository)
        .<DictionaryWord, DictionaryWord>chunk(100, transactionManager)
        .reader(wordJdbcPagingItemReader)
        .processor(wordScrapingProcessor)
        .writer(wordScrapingWriter)
        .faultTolerant()
        .skip(Exception.class)
        .skipLimit(Integer.MAX_VALUE)
        .listener(wordScrapingStepListener)
        .build();
  }

  @Bean
  @StepScope
  JdbcPagingItemReader<DictionaryWord> wordJdbcPagingItemReader(
      @Value("#{stepExecutionContext['minId']}") Integer minId,
      @Value("#{stepExecutionContext['maxId']}") Integer maxId,
      DataSource dataSource)
      throws Exception {
    SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
    queryProvider.setDataSource(dataSource);
    queryProvider.setSelectClause("id, source, word, lexical_entry, status");
    queryProvider.setFromClause("dictionary_words");
    queryProvider.setWhereClause(
        "(id >= :minId AND id <= :maxId) AND (status = :createdStatus OR status = :failedStatus)");
    queryProvider.setSortKey("id");

    return new JdbcPagingItemReaderBuilder<DictionaryWord>()
        .name("wordJdbcPagingItemReader")
        .dataSource(dataSource)
        .rowMapper(new DictionaryWordRowMapper())
        .queryProvider(queryProvider.getObject())
        .parameterValues(
            Map.of(
                "minId",
                minId,
                "maxId",
                maxId,
                "createdStatus",
                StatusType.CREATED.name(),
                "failedStatus",
                StatusType.FAILED.name()))
        .build();
  }

  @Bean
  WordScrapingProcessor wordScrapingProcessor(
      JdbcDictionaryWordDao dictionaryWordDao, Scraper scraper) {
    WordScrapingProcessor processor = new WordScrapingProcessor();
    processor.setDictionaryWordDao(dictionaryWordDao);
    processor.setScraper(scraper);
    return processor;
  }

  @Bean
  WordScrapingWriter wordScrapingWriter(JdbcDictionaryWordDao dictionaryWordDao) throws Exception {
    WordScrapingWriter writer = new WordScrapingWriter();
    writer.setDictionaryWordDao(dictionaryWordDao);
    writer.afterPropertiesSet();
    return writer;
  }

  @Bean
  WordScrapingStepListener wordScrapingStepListener(JdbcDictionaryWordDao dictionaryWordDao) {
    WordScrapingStepListener listener = new WordScrapingStepListener();
    listener.setDictionaryWordDao(dictionaryWordDao);
    return listener;
  }

  @Bean
  JdbcDictionaryWordDao dictionaryWordDao(DataSource dataSource) {
    JdbcDictionaryWordDao dictionaryWordDao = new JdbcDictionaryWordDao();
    dictionaryWordDao.setDataSource(dataSource);
    return dictionaryWordDao;
  }

  @Bean
  Scraper scraper() {
    return new DefaultScraper();
  }
}
