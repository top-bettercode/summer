package top.bettercode.simpleframework.web.error;

import top.bettercode.simpleframework.web.RespEntity;
import java.util.Map;

/**
 * @author Peter Wu
 */
public interface IErrorHandler {

  /**
   * 处理异常
   *
   * @param error      异常
   * @param respEntity 响应容器
   * @param errors     错误
   * @param separator  属性异常分隔符
   */
  void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator);

  /**
   * 得到国际化信息 未找到时返回代码 code
   *
   * @param code 模板
   * @param args 参数
   * @return 信息
   */
  String getText(Object code, Object... args);
}
