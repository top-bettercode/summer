package top.bettercode.simpleframework.web.error;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.StringUtils;
import top.bettercode.simpleframework.web.RespEntity;

/**
 * @author Peter Wu
 */
public class DataErrorHandler extends AbstractErrorHandler {


  public DataErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    String message = null;
    if (error instanceof org.springframework.transaction.TransactionSystemException) {//数据验证
      error = ((TransactionSystemException) error).getRootCause();

      if (error instanceof ConstraintViolationException) {
        constraintViolationException((ConstraintViolationException) error, respEntity, errors,
            separator);
      }
    } else if (error instanceof DataIntegrityViolationException) {
      String specificCauseMessage = ((DataIntegrityViolationException) error).getMostSpecificCause()
          .getMessage();
      String duplicateRegex = "^Duplicate entry '(.*?)'.*";
      String dataTooLongRegex = "^Data truncation: Data too long for column '(.*?)'.*";
      String outOfRangeRegex = "^Data truncation: Out of range value for column '(.*?)'.*";
      String constraintSubfix = "Cannot delete or update a parent row";
      if (specificCauseMessage.matches(duplicateRegex)) {
        String columnName = getText(
            specificCauseMessage.replaceAll(duplicateRegex, "$1"));
        message = getText("duplicate.entry", columnName);
        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (specificCauseMessage.matches(dataTooLongRegex)) {
        String columnName = getText(
            specificCauseMessage.replaceAll(dataTooLongRegex, "$1"));
        message = getText("data.too.long", columnName);
        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (specificCauseMessage.matches(outOfRangeRegex)) {
        String columnName = getText(
            specificCauseMessage.replaceAll(outOfRangeRegex, "$1"));
        message = getText("data Out of range", columnName);
        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (specificCauseMessage.startsWith(constraintSubfix)) {
        message = "cannot.delete.update.parent";
        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else {
        message = ((DataIntegrityViolationException) error).getRootCause().getMessage();
      }
    } else if (error instanceof UncategorizedSQLException) {
      String detailMessage = ((UncategorizedSQLException) error).getSQLException().getMessage()
          .trim();
      String regex = ".*ORA-12899: .*\\..*\\.\"(.*?)\" 的值太大 \\(实际值: \\d+, 最大值: (\\d+)\\)";
      //ORA-12899: value too large for column "YUNTUDEV"."PU_DELIVERY_ORDER"."LICENSE" (actual: 47, maximum: 30)
      String regex1 = ".*ORA-12899: value too large for column .*\\..*\\.\"(.*?)\" \\(actual: \\d+, maximum: (\\d+)\\)";
      //Incorrect string value: '\xF0\x9F\x98\x84\xF0\x9F...' for column 'remark' at row 1
      if (detailMessage.matches("^Incorrect string value: '.*\\\\xF0.*$")) {
        message = "datasource.incorrect.emoji";
        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
      } else if (detailMessage.matches(regex)) {
        String field = detailMessage.replaceAll(regex, "$1");
        String maxLeng = detailMessage.replaceAll(regex, "$2");
        message = getText(field) + "长度不能大于" + maxLeng;
        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
      } else if (detailMessage.matches(regex1)) {
        String field = detailMessage.replaceAll(regex1, "$1");
        String maxLeng = detailMessage.replaceAll(regex1, "$2");
        message = getText(field) + "长度不能大于" + maxLeng;
        respEntity.setHttpStatusCode(HttpStatus.BAD_REQUEST.value());
      } else {
        message = detailMessage;
      }
    } else if (error instanceof DataAccessResourceFailureException) {
      Throwable cause = error.getCause();
      String extractMessage = "could not extract ResultSet";
      if (cause != null && extractMessage.equals(cause.getMessage())) {
        message = cause.getCause() != null ? cause.getCause().getMessage() : extractMessage;
      }
      String errorMessage = error.getMessage();
      if (errorMessage != null) {
        if (errorMessage.contains("Socket read timed out")) {
          message = "datasource.request.timeout";
        }
        if (errorMessage.contains("Unable to acquire JDBC Connection")) {
          message = "Unable to acquire JDBC Connection";
        }
      }
    }
    if (StringUtils.hasText(message)) {
      respEntity.setMessage(message);
    }
  }
}
