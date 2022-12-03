package top.bettercode.summer.tools.excel;

/**
 * @author Peter Wu
 */
public class ExcelException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ExcelException(String message) {
    super(message);
  }

  public ExcelException(String message, Throwable cause) {
    super(message, cause);
  }
}
