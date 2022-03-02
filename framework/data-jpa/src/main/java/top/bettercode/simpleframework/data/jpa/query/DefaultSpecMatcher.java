package top.bettercode.simpleframework.data.jpa.query;

/**
 * @author Peter Wu
 */
public class DefaultSpecMatcher<T> extends SpecMatcher<T, DefaultSpecMatcher<T>> {

  private static final long serialVersionUID = 1L;

  protected DefaultSpecMatcher(SpecMatcherMode matcherMode, T probe) {
    super(matcherMode, probe);
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @param <T> T
   * @return SpecMatcher 实例
   */
  public static <T> SpecMatcher<T, DefaultSpecMatcher<T>> matching() {
    return new DefaultSpecMatcher<>(SpecMatcherMode.ALL, null);
  }

  /**
   * 创建 SpecMatcher 实例
   *
   * @param <T> T
   * @return SpecMatcher 实例
   */
  public static <T> SpecMatcher<T, DefaultSpecMatcher<T>> matchingAny() {
    return new DefaultSpecMatcher<>(SpecMatcherMode.ANY, null);
  }

}
