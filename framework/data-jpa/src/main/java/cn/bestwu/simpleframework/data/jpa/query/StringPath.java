package cn.bestwu.simpleframework.data.jpa.query;

import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatcher;
import org.springframework.data.domain.ExampleMatcher.PropertyValueTransformer;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;

/**
 * @author Peter Wu
 */
public class StringPath<T, E extends TypedExample<T>> extends ExactPath<T, E> {

  public StringPath(E example, String propertyPath) {
    super(example, propertyPath);
  }

  /**
   * Sets ignores case to {@literal true}.
   *
   * @return E
   */
  public E ignoreCase() {
    return withMatcher(GenericPropertyMatcher::ignoreCase);
  }

  /**
   * Sets ignores case to {@code ignoreCase}.
   *
   * @param ignoreCase ignoreCase
   * @return E
   */
  public E ignoreCase(boolean ignoreCase) {
    return withMatcher(match -> match.ignoreCase(ignoreCase));
  }

  /**
   * Sets ignores case to {@literal false}.
   *
   * @return E
   */
  public E caseSensitive() {
    return withMatcher(GenericPropertyMatcher::caseSensitive);
  }

  /**
   * Sets string matcher to {@link StringMatcher#CONTAINING}.
   *
   * @return E
   */
  public E contains() {
    return withMatcher(GenericPropertyMatcher::contains);
  }

  /**
   * Sets string matcher to {@link StringMatcher#CONTAINING}.
   *
   * @param params 参数值
   * @return E
   */
  public E contains(Object params) {
    setValue(params);
    return contains();
  }

  /**
   * Sets string matcher to {@link StringMatcher#ENDING}.
   *
   * @return E
   */
  public E endsWith() {
    return withMatcher(GenericPropertyMatcher::endsWith);
  }

  /**
   * Sets string matcher to {@link StringMatcher#ENDING}.
   *
   * @param params 参数值
   * @return E
   */
  public E endsWith(Object params) {
    setValue(params);
    return endsWith();
  }

  /**
   * Sets string matcher to {@link StringMatcher#STARTING}.
   *
   * @return E
   */
  public E startsWith() {
    return withMatcher(GenericPropertyMatcher::startsWith);
  }

  /**
   * Sets string matcher to {@link StringMatcher#STARTING}.
   *
   * @param params 参数值
   * @return E
   */
  public E startsWith(Object params) {
    setValue(params);
    return startsWith();
  }

  /**
   * Sets string matcher to {@link StringMatcher#DEFAULT}.
   *
   * @return E
   */
  public E storeDefaultMatching() {
    return withMatcher(GenericPropertyMatcher::storeDefaultMatching);
  }

  /**
   * Sets string matcher to {@link StringMatcher#DEFAULT}.
   *
   * @param params 参数值
   * @return E
   */
  public E storeDefaultMatching(Object params) {
    setValue(params);
    return storeDefaultMatching();
  }

  /**
   * Sets string matcher to {@link StringMatcher#REGEX}.
   *
   * @return E
   */
  public E regex() {
    return withMatcher(GenericPropertyMatcher::regex);
  }

  /**
   * Sets string matcher to {@link StringMatcher#REGEX}.
   *
   * @param params 参数值
   * @return E
   */
  public E regex(Object params) {
    setValue(params);
    return regex();
  }

  /**
   * Sets string matcher to {@code stringMatcher}.
   *
   * @param stringMatcher must not be {@literal null}.
   * @return E
   */
  public E stringMatcher(StringMatcher stringMatcher) {
    return withMatcher(match -> match.stringMatcher(stringMatcher));
  }

  /**
   * Sets the {@link PropertyValueTransformer} to {@code propertyValueTransformer}.
   *
   * @param propertyValueTransformer must not be {@literal null}.
   * @return E
   */
  public E transform(PropertyValueTransformer propertyValueTransformer) {
    return withMatcher(match -> match.transform(propertyValueTransformer));
  }
}
