package top.bettercode.simpleframework.data.jpa;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.web.RespEntity;
import top.bettercode.simpleframework.web.error.AbstractErrorHandler;

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
      Throwable cause = error.getCause();
      if (cause != null) {
        String causeMessage = cause.getMessage();
        if (causeMessage != null) {
          message = causeMessage.trim();
//ORA-12899: 列 "YUNTUDEV"."PU_ASK_SEND_TMS"."FROM_ADDRESS" 的值太大 (实际值: 1421, 最大值: 600)
          String regex = ".*ORA-12899: .*\\..*\\.\"(.*?)\" 的值太大 \\(实际值: \\d+, 最大值: (\\d+)\\)";
          if (message.matches(regex)) {
            String field = message.replaceAll(regex, "$1");
            String maxLeng = message.replaceAll(regex, "$2");
            message = getText(field) + "长度不能大于" + maxLeng;
            respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
          }
          //ORA-12899: value too large for column "YUNTUDEV"."PU_DELIVERY_ORDER"."LICENSE" (actual: 47, maximum: 30)
          regex = ".*ORA-12899: value too large for column .*\\..*\\.\"(.*?)\" \\(actual: \\d+, maximum: (\\d+)\\)";
          if (message.matches(regex)) {
            String field = message.replaceAll(regex, "$1");
            String maxLeng = message.replaceAll(regex, "$2");
            message = getText(field) + "长度不能大于" + maxLeng;
            respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
          }
        }
      }
    } else if (error instanceof MyBatisSystemException) {
      if (error.getMessage()
          .contains("Cause: org.springframework.jdbc.CannotGetJdbcConnectionException")) {
        message = getText("datasource.request.timeout");
      }
    }
    if (StringUtils.hasText(message)) {
      respEntity.setMessage(message);
    }
  }
}