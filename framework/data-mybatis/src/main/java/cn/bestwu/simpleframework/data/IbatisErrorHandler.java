package cn.bestwu.simpleframework.data;

import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.error.AbstractErrorHandler;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public class IbatisErrorHandler extends AbstractErrorHandler {


  public IbatisErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    String message = null;
    if (error instanceof PersistenceException) {
      message = error.getCause().getMessage().trim();
//ORA-12899: 列 "YUNTUDEV"."PU_ASK_SEND_TMS"."FROM_ADDRESS" 的值太大 (实际值: 1421, 最大值: 600)
      String regex = "ORA-12899: .*\\..*\\.\"(.*?)\" 的值太大 \\(实际值: \\d+, 最大值: (\\d+)\\)";
      if (message.matches(regex)) {
        String field = message.replaceAll(regex, "$1");
        String maxLeng = message.replaceAll(regex, "$2");
        message = getText(field) + "长度不能大于" + maxLeng;
        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
      }
    }
    if (StringUtils.hasText(message)) {
      respEntity.setMessage(message);
    }
  }
}
