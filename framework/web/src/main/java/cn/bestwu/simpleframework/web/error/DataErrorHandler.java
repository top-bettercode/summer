package cn.bestwu.simpleframework.web.error;

import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.validator.NoPropertyPath;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public class DataErrorHandler implements IErrorHandler {

  @Autowired
  private MessageSource messageSource;
  @Autowired(required = false)
  private HttpServletRequest request;

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors) {
    String message = null;
    if (error instanceof org.springframework.transaction.TransactionSystemException) {//数据验证
      error = ((TransactionSystemException) error).getRootCause();

      if (error instanceof ConstraintViolationException) {
        respEntity.setHttpStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());

        ConstraintViolationException er = (ConstraintViolationException) error;
        Set<ConstraintViolation<?>> constraintViolations = er.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
          String property = ErrorAttributes.getProperty(constraintViolation);
          String msg;
          if (constraintViolation.getConstraintDescriptor().getPayload()
              .contains(NoPropertyPath.class)) {
            msg = constraintViolation.getMessage();
          } else {
            msg =
                getText(messageSource, request, property) + ": " + constraintViolation.getMessage();
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
        String columnName = getText(messageSource, request,
            specificCauseMessage.replaceAll(duplicateRegex, "$1"));
        message = getText(messageSource, request, "duplicate.entry", columnName);
        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (specificCauseMessage.matches(dataTooLongRegex)) {
        String columnName = getText(messageSource, request,
            specificCauseMessage.replaceAll(dataTooLongRegex, "$1"));
        message = getText(messageSource, request, "data.too.long", columnName);
        if (!StringUtils.hasText(message)) {
          message = "data.valid.failed";
        }
      } else if (specificCauseMessage.matches(outOfRangeRegex)) {
        String columnName = getText(messageSource, request,
            specificCauseMessage.replaceAll(outOfRangeRegex, "$1"));
        message = getText(messageSource, request, "data Out of range", columnName);
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
