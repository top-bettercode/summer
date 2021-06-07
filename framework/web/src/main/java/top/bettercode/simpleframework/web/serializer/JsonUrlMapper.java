package top.bettercode.simpleframework.web.serializer;

/**
 * @author Peter Wu
 */
public interface JsonUrlMapper {

  /**
   * @param obj 对象
   * @return 字符串
   */
  default String mapper(Object obj) {
    return (obj == null) ? null : obj.toString().trim();
  }

}
