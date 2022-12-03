package top.bettercode.summer.test;

/**
 * @author Peter Wu
 */
public interface AutoDocRequestHandler {

  void handle(AutoDocHttpServletRequest request);

  default boolean support(AutoDocHttpServletRequest request) {
    return true;
  }

}
