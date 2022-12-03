package top.bettercode.summer.data.jpa.support;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.util.StringUtils;
import top.bettercode.summer.web.RespEntity;
import top.bettercode.summer.web.error.AbstractErrorHandler;

/**
 * @author Peter Wu
 */
public class DataJpaErrorHandler extends AbstractErrorHandler {


  public DataJpaErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    String message = null;
    if (error instanceof JpaSystemException) {
      Throwable cause = error.getCause();
      if (cause != null) {
        if (cause instanceof org.hibernate.exception.GenericJDBCException) {
          cause = cause.getCause() != null ? cause.getCause() : cause;
        }
        String causeMessage = cause.getMessage();
        if (causeMessage != null) {
          message = causeMessage.trim();
//ORA-12899: 列 "YUNTUDEV"."PU_ASK_SEND_TMS"."FROM_ADDRESS" 的值太大 (实际值: 1421, 最大值: 600)
          String regex = ".*ORA-12899: .*\\..*\\.\"(.*?)\" 的值太大 \\(实际值: \\d+, 最大值: (\\d+)\\)";
          //ORA-12899: value too large for column "YUNTUDEV"."PU_DELIVERY_ORDER"."LICENSE" (actual: 47, maximum: 30)
          String regex1 = ".*ORA-12899: value too large for column .*\\..*\\.\"(.*?)\" \\(actual: \\d+, maximum: (\\d+)\\)";
          if (message.matches(regex)) {
            String field = message.replaceAll(regex, "$1");
            String maxLeng = message.replaceAll(regex, "$2");
            message = getText(field) + "长度不能大于" + maxLeng;
            respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
          } else if (message.matches(regex1)) {
            String field = message.replaceAll(regex1, "$1");
            String maxLeng = message.replaceAll(regex1, "$2");
            message = getText(field) + "长度不能大于" + maxLeng;
            respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
          }
        }
      }
    } else if (error instanceof InvalidDataAccessApiUsageException) {
      message = error.getMessage();
      if (message != null && message.contains("detached entity passed to persist")) {
        message = "更新的数据在数据库中不存在";
      }
    }
    if (StringUtils.hasText(message)) {
      respEntity.setMessage(message);
    }
  }
}
