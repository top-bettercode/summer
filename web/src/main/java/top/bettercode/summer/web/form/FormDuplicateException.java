package top.bettercode.summer.web.form;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FormDuplicateException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FormDuplicateException(String message) {
    super(message);
  }

}
