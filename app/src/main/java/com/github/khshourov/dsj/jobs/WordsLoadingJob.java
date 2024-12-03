package com.github.khshourov.dsj.jobs;

import com.github.khshourov.dsj.db.datasource.EmbeddedDataSourceConfiguration;
import com.github.khshourov.dsj.db.datasource.PersistentDataSourceConfiguration;
import com.github.khshourov.dsj.models.DictionaryWord;
import com.github.khshourov.dsj.processors.WordItemProcessor;
import com.github.khshourov.dsj.readers.WordItemReader;
import com.github.khshourov.dsj.writers.WordItemWriter;
import java.util.Arrays;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
@EnableBatchProcessing
@Import({PersistentDataSourceConfiguration.class, EmbeddedDataSourceConfiguration.class})
public class WordsLoadingJob {
  @Bean
  public Job job(JobRepository jobRepository, Step wordsLoadingStep) {
    return new JobBuilder("wordsLoadingJob", jobRepository).start(wordsLoadingStep).build();
  }

  @Bean
  public Step wordsLoadingStep(
      JobRepository jobRepository,
      JdbcTransactionManager transactionManager,
      WordItemReader itemReader,
      WordItemProcessor itemProcessor,
      WordItemWriter itemWriter) {
    return new StepBuilder("wordsLoadingStep", jobRepository)
        .<DictionaryWord, DictionaryWord>chunk(100, transactionManager)
        .reader(itemReader)
        .processor(itemProcessor)
        .writer(itemWriter)
        .faultTolerant()
        .skip(IllegalArgumentException.class)
        .skipLimit(Integer.MAX_VALUE)
        .build();
  }

  @Bean
  @StepScope
  public WordItemReader itemReader(
      @Value("#{jobParameters['sources']}") String sources,
      MultiResourceItemReader<String> delegateItemReader)
      throws Exception {
    WordItemReader itemReader = new WordItemReader();
    itemReader.setDelegate(delegateItemReader);
    itemReader.setSources(Arrays.stream(sources.split(",")).toList());
    itemReader.afterPropertiesSet();
    return itemReader;
  }

  @Bean
  @StepScope
  public MultiResourceItemReader<String> delegateItemReader(
      @Value("#{jobParameters['wordFiles']}") Resource[] resources,
      FlatFileItemReader<String> flatFileItemReader) {
    return new MultiResourceItemReaderBuilder<String>()
        .name("multiSourcesItemReader")
        .resources(resources)
        .delegate(flatFileItemReader)
        .build();
  }

  @Bean
  public FlatFileItemReader<String> flatFileItemReader() {
    return new FlatFileItemReaderBuilder<String>()
        .name("delegateItemReader")
        .lineMapper((line, lineNumber) -> line)
        .build();
  }

  @Bean
  public WordItemProcessor itemProcessor(DataSource dataSource) throws Exception {
    WordItemProcessor itemProcessor = new WordItemProcessor();
    itemProcessor.setDataSource(dataSource);
    itemProcessor.afterPropertiesSet();
    return itemProcessor;
  }

  @Bean
  public WordItemWriter itemWriter(DataSource dataSource) throws Exception {
    WordItemWriter itemWriter = new WordItemWriter();
    itemWriter.setDataSource(dataSource);
    itemWriter.afterPropertiesSet();
    return itemWriter;
  }
}
