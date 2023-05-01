package top.bettercode.summer.tools.excel;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.excel.ExcelImportException.CellError;
import top.bettercode.summer.web.RespEntity;
import top.bettercode.summer.web.error.AbstractErrorHandler;

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
            errors.put(key, title + ": " + constraintViolation.getMessage());
          }
        } else {
          String msg = value.getMessage();
          if (value instanceof DateTimeParseException) {
            String msgRegex = "Text '(.*?)' could not be parsed at index (\\d+)";
            if (msg.matches(msgRegex)) {
              msg = msg.replaceAll(msgRegex, "$1") + "不是有效的日期格式";
            }
          }
          errors.put(key, title + ": " + getText(msg));
        }
      }
      Entry<String, String> firstError = errors.entrySet().iterator().next();
      message = firstError.getKey() + separator + firstError.getValue();
    }
    if (StringUtils.hasText(message)) {
      respEntity.message = message;
    }
  }
}
