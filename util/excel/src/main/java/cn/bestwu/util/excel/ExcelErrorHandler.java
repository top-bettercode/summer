package cn.bestwu.util.excel;

import cn.bestwu.util.excel.ExcelImportException.CellError;
import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.error.IErrorHandler;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public class ExcelErrorHandler implements IErrorHandler {

  @Autowired
  private MessageSource messageSource;
  @Autowired(required = false)
  private HttpServletRequest request;

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors) {
    String message = null;
    if (error instanceof ExcelImportException) {
      List<CellError> cellErrors = ((ExcelImportException) error).getErrors();

      for (CellError cellError : cellErrors) {
        String key = getText(messageSource, request, cellError.getMessage(), cellError.getRow(),
            cellError.getColumnName());
        String title = cellError.getTitle();
        Exception value = cellError.getException();
        if (value instanceof ConstraintViolationException) {
          for (ConstraintViolation<?> constraintViolation : ((ConstraintViolationException) value)
              .getConstraintViolations()) {
            errors.put(key, title + constraintViolation.getMessage());
          }
        } else {
          errors.put(key, title + getText(messageSource, request, value.getMessage()));
        }
      }
      Entry<String, String> firstError = errors.entrySet().iterator().next();
      message = firstError.getKey() + ":" + firstError.getValue();
    }
    if (StringUtils.hasText(message)) {
      respEntity.setMessage(message);
    }
  }
}
