package top.bettercode.summer.tools.sap.connection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Peter Wu
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SapSysException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SapSysException(String message) {
    super("SAP系统：" + message);
  }

}
