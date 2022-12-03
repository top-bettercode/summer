package top.bettercode.summer.data.jpa.query;

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

  public static <T> SpecMatcher<T, DefaultSpecMatcher<T>> matching(T probe) {
    return new DefaultSpecMatcher<>(SpecMatcherMode.ALL, probe);
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

  public static <T> SpecMatcher<T, DefaultSpecMatcher<T>> matchingAny(T probe) {
    return new DefaultSpecMatcher<>(SpecMatcherMode.ANY, probe);
  }

}
