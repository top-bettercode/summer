package cn.bestwu.simpleframework.web.error;

import cn.bestwu.simpleframework.web.RespEntity;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;

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
   * @param messageSource messageSource
   * @param request       webRequest
   * @param code          模板
   * @param args          参数
   * @return 信息
   */
  default String getText(MessageSource messageSource, HttpServletRequest request, Object code,
      Object... args) {
    String codeString = String.valueOf(code);
    return messageSource.getMessage(codeString, args, codeString,
        request == null ? Locale.CHINA : request.getLocale());
  }
}
