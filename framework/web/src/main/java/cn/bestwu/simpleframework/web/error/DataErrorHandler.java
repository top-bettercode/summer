package cn.bestwu.simpleframework.web.error;

import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.validator.NoPropertyPath;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.StringUtils;

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
        respEntity.setHttpStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());

        ConstraintViolationException er = (ConstraintViolationException) error;
        Set<ConstraintViolation<?>> constraintViolations = er.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
          String property = getProperty(constraintViolation);
          String msg;
          if (constraintViolation.getConstraintDescriptor().getPayload()
              .contains(NoPropertyPath.class)) {
            msg = constraintViolation.getMessage();
          } else {
            msg =
                getText(property) + separator + constraintViolation
                    .getMessage();
          }
          errors.put(property, msg);
        }
        message = errors.values().iterator().next();

        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
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
      String detailMessage = ((UncategorizedSQLException) error).getSQLException().getMessage();
      //Incorrect string value: '\xF0\x9F\x98\x84\xF0\x9F...' for column 'remark' at row 1
      if (detailMessage.matches("^Incorrect string value: '.*\\\\xF0.*$")) {
        message = "datasource.incorrect.emoji";
      } else {
        message = detailMessage;
      }
    }
    if (StringUtils.hasText(message)) {
      respEntity.setMessage(message);
    }
  }
}
