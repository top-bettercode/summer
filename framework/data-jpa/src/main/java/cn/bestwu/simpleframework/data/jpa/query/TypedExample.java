package cn.bestwu.simpleframework.data.jpa.query;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

public class TypedExample<T> implements Example<T> {

  private final T probe;
  private final ExampleMatcher matcher;

  protected TypedExample(T probe) {
    this.probe = probe;
    this.matcher = ExampleMatcher.matching();
  }

  @Override
  public T getProbe() {
    return probe;
  }

  @Override
  public ExampleMatcher getMatcher() {
    return matcher;
  }

}