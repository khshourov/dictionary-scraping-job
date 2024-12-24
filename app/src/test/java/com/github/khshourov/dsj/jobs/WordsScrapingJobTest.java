package com.github.khshourov.dsj.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import com.github.khshourov.dsj.db.DictionaryWordRowMapper;
import com.github.khshourov.dsj.db.StatusType;
import com.github.khshourov.dsj.models.DictionaryWord;
import com.github.khshourov.dsj.scraper.Scraper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.jdbc.JdbcTestUtils;

@SpringBootTest(classes = {WordsScrapingJob.class})
class WordsScrapingJobTest {
  private static final String source = "source";
  private static final String word = "word";
  private static final String scrapedSerializedData = "scrapedSerializedData";

  @Autowired private JobLauncher jobLauncher;

  @Autowired private Job job;

  @Autowired private DataSource dataSource;

  @MockitoBean private Scraper scraper;

  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void init() {
    jdbcTemplate = new JdbcTemplate(dataSource);
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "dictionary_words");
  }

  @ParameterizedTest
  @MethodSource("scrapableStatus")
  void scrapingSucceedWithValidWordAndScrapableState(StatusType scrapableStatus)
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {
    jdbcTemplate.update(
        "INSERT INTO dictionary_words(source, word, status) VALUES(?, ?, ?)",
        source,
        word,
        scrapableStatus.name());
    when(scraper.scrape(word, source)).thenReturn(Optional.of(scrapedSerializedData));

    JobExecution jobExecution =
        jobLauncher.run(
            job,
            new JobParametersBuilder()
                .addString("timestamp", Instant.now().toString())
                .toJobParameters());

    assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    DictionaryWord dictionaryWord = findBySourceAndWord(source, word, jdbcTemplate);

    assertNotNull(dictionaryWord);
    assertEquals(StatusType.SCRAPED, dictionaryWord.status());
    assertEquals(scrapedSerializedData, dictionaryWord.lexicalEntry());
  }

  static Stream<Arguments> scrapableStatus() {
    return Stream.of(arguments(StatusType.CREATED), arguments(StatusType.FAILED));
  }

  @ParameterizedTest
  @MethodSource("nonScrapableStatus")
  void scrapingSkippedWithValidWordAndNonScrapableState(StatusType nonScrapableStatus)
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {
    jdbcTemplate.update(
        "INSERT INTO dictionary_words(source, word, status) VALUES(?, ?, ?)",
        source,
        word,
        nonScrapableStatus.name());
    when(scraper.scrape(word, source)).thenReturn(Optional.of(scrapedSerializedData));

    JobExecution jobExecution =
        jobLauncher.run(
            job,
            new JobParametersBuilder()
                .addString("timestamp", Instant.now().toString())
                .toJobParameters());

    assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    assertEquals(0, jobExecution.getStepExecutions().stream().findFirst().get().getReadCount());
    DictionaryWord dictionaryWord = findBySourceAndWord(source, word, jdbcTemplate);

    assertNotNull(dictionaryWord);
    assertEquals(nonScrapableStatus, dictionaryWord.status());
  }

  static Stream<Arguments> nonScrapableStatus() {
    return Stream.of(
        arguments(StatusType.SCRAPING),
        arguments(StatusType.SCRAPED),
        arguments(StatusType.NOT_FOUND));
  }

  @Test
  void statusShouldBeNotFoundIfScrapingReturnsEmpty()
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {
    jdbcTemplate.update(
        "INSERT INTO dictionary_words(source, word, status) VALUES(?, ?, ?)",
        source,
        word,
        StatusType.CREATED.name());
    when(scraper.scrape(word, source)).thenReturn(Optional.empty());

    JobExecution jobExecution =
        jobLauncher.run(
            job,
            new JobParametersBuilder()
                .addString("timestamp", Instant.now().toString())
                .toJobParameters());

    assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    DictionaryWord dictionaryWord = findBySourceAndWord(source, word, jdbcTemplate);

    assertNotNull(dictionaryWord);
    assertEquals(StatusType.NOT_FOUND, dictionaryWord.status());
  }

  private DictionaryWord findBySourceAndWord(
      String source, String word, JdbcTemplate jdbcTemplate) {
    List<DictionaryWord> dictionaryWords =
        jdbcTemplate.query(
            "SELECT * FROM dictionary_words WHERE source = ? AND word = ?",
            new DictionaryWordRowMapper(),
            source,
            word);
    return dictionaryWords.isEmpty() ? null : dictionaryWords.getFirst();
  }
}
