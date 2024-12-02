package com.github.khshourov.dsj.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

public class WordsLoadingJobTest {
  @Test
  void testJobLaunch()
      throws JobInstanceAlreadyCompleteException,
          JobExecutionAlreadyRunningException,
          JobParametersInvalidException,
          JobRestartException {
    ApplicationContext applicationContext =
        new AnnotationConfigApplicationContext(WordsLoadingJob.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);
    Job job = applicationContext.getBean(Job.class);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(applicationContext.getBean(DataSource.class));

    JobExecution jobExecution =
        jobLauncher.run(
            job,
            new JobParametersBuilder()
                .addString("timestamp", Instant.now().toString())
                .addString("wordFiles", "com/github/khshourov/dsj/words/words-*.txt")
                .addString("sources", "source1,source2")
                .toJobParameters());

    assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

    int createdEntries =
        JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "dictionary_words", "status = 'CREATED'");
    assertEquals(40, createdEntries);

    int source1Entries =
        JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "dictionary_words", "status = 'CREATED' AND source = 'source1'");
    assertEquals(20, source1Entries);

    int source2Entries =
        JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "dictionary_words", "status = 'CREATED' AND source = 'source2'");
    assertEquals(20, source2Entries);

    int uniqueWords =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT word) FROM dictionary_words", Integer.class);
    assertEquals(20, uniqueWords);

    jobExecution =
        jobLauncher.run(
            job,
            new JobParametersBuilder()
                .addString("timestamp", Instant.now().toString())
                .addString("wordFiles", "com/github/khshourov/dsj/words/words-*.txt")
                .addString("sources", "source1,source2")
                .toJobParameters());
    assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    assertEquals(40, jobExecution.getStepExecutions().stream().findFirst().get().getSkipCount());
  }
}
