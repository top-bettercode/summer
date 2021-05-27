package cn.bestwu.util.excel;

import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.error.AbstractErrorHandler;
import cn.bestwu.util.excel.ExcelImportException.CellError;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public class ExcelErrorHandler extends AbstractErrorHandler {


  public ExcelErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    super(messageSource, request);
  }

  @Override
  public void handlerException(Throwable error, RespEntity<?> respEntity,
      Map<String, String> errors, String separator) {
    String message = null;
    if (error instanceof ExcelImportException) {
      List<CellError> cellErrors = ((ExcelImportException) error).getErrors();

      for (CellError cellError : cellErrors) {
        String key = getText(cellError.getMessage(), cellError.getRow(),
            cellError.getColumnName());
        String title = cellError.getTitle();
        Exception value = cellError.getException();
        if (value instanceof ConstraintViolationException) {
          for (ConstraintViolation<?> constraintViolation : ((ConstraintViolationException) value)
              .getConstraintViolations()) {
            errors.put(key, title + constraintViolation.getMessage());
          }
        } else {
          errors.put(key, title + getText(value.getMessage()));
        }
      }
      Entry<String, String> firstError = errors.entrySet().iterator().next();
      message = firstError.getKey() + separator + firstError.getValue();
    }
    if (StringUtils.hasText(message)) {
      respEntity.setMessage(message);
    }
  }
}
