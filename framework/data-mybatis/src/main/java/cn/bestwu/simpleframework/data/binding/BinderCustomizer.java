package cn.bestwu.simpleframework.data.binding;

/**
 * 自定义Binder
 *
 * @author Peter Wu
 */
public interface BinderCustomizer<E> {

  /**
   * @param wrapper 待处理wrapper
   */
  void customize(E wrapper);

}
