package com.github.khshourov.dsj;

import com.github.khshourov.dsj.jobs.WordsLoadingJob;
import com.github.khshourov.dsj.jobs.WordsScrapingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class DictionaryScrapingApplication {
  private static final Logger log = LoggerFactory.getLogger(DictionaryScrapingApplication.class);

  public static void main(String[] args) throws InterruptedException {
    try (AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(WordsLoadingJob.class, WordsScrapingJob.class)) {
      JobLauncher jobLauncher = context.getBean(JobLauncher.class);

      Job loadingJob = context.getBean("loadingJob", Job.class);
      Job scrapingJob = context.getBean("scrapingJob", Job.class);

      jobLauncher.run(
          loadingJob,
          new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());

      jobLauncher.run(
          scrapingJob,
          new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    Thread.sleep(600000);
  }
}
