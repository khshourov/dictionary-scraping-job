package com.github.khshourov.dsj.readers;

import com.github.khshourov.dsj.models.Word;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.InitializingBean;

public class WordItemReader implements ItemReader<Word>, ItemStream, InitializingBean {
  private Queue<Word> buffer = new LinkedList<>();

  private MultiResourceItemReader<String> delegate;
  private List<String> sources;

  @Override
  public Word read()
      throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    if (buffer.isEmpty()) {
      String line = this.delegate.read();
      if (line == null) {
        return null;
      }

      for (String source : sources) {
        buffer.add(new Word(source, line));
      }
    }

    return buffer.poll();
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    this.delegate.open(executionContext);
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    this.delegate.update(executionContext);
  }

  @Override
  public void close() throws ItemStreamException {
    this.delegate.close();
  }

  public void setDelegate(MultiResourceItemReader<String> delegate) {
    this.delegate = delegate;
  }

  public void setSources(List<String> sources) {
    this.sources = sources;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.delegate == null) {
      throw new IllegalStateException("delegate needs to be set");
    }

    if (this.sources == null || this.sources.isEmpty()) {
      throw new IllegalStateException("sources need to be set");
    }
  }
}
