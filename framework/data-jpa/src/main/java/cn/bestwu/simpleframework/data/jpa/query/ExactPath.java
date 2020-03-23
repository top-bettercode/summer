package cn.bestwu.simpleframework.data.jpa.query;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatcher;
import org.springframework.data.domain.ExampleMatcher.MatcherConfigurer;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;

/**
 * @author Peter Wu
 */
public class ExactPath<T, E extends TypedExample<T>> implements Example<T> {

  private final E example;
  private final String propertyPath;
  private final PropertyDescriptor propertyDescriptor;

  public ExactPath(E example, String propertyPath) {
    this.example = example;
    this.propertyPath = propertyPath;
    this.propertyDescriptor = BeanUtils.getPropertyDescriptor(example.getProbeType(), propertyPath);
  }

  @Override
  public T getProbe() {
    return example.getProbe();
  }

  @Override
  public ExampleMatcher getMatcher() {
    return example.getMatcher();
  }

  //--------------------------------------------

  protected E withMatcher(MatcherConfigurer<GenericPropertyMatcher> matcherConfigurer) {
    getMatcher().withMatcher(propertyPath, matcherConfigurer);
    return example;
  }

  protected void setValue(Object params) {
    try {
      propertyDescriptor.getWriteMethod().invoke(getProbe(), params);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
  //--------------------------------------------

  /**
   * Sets string matcher to {@link StringMatcher#EXACT}.
   *
   * @return E
   */
  public E exact() {
    return withMatcher(GenericPropertyMatcher::exact);
  }

  /**
   * Sets string matcher to {@link StringMatcher#EXACT}.
   *
   * @param params 参数值
   * @return E
   */
  public E exact(Object params) {
    setValue(params);
    return withMatcher(GenericPropertyMatcher::exact);
  }

}
