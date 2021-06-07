package top.bettercode.util.excel;

/**
 * @author Peter Wu
 */
public class ExcelException extends RuntimeException {

  private static final long serialVersionUID = -6806745108191259515L;

  public ExcelException(String message) {
    super(message);
  }

  public ExcelException(String message, Throwable cause) {
    super(message, cause);
  }
}
