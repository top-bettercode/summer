package top.bettercode.summer.tools.sap.connection;

/**
 * @author Peter Wu
 */
public class SapException extends RuntimeException {

  private static final long serialVersionUID = 1L;


  public SapException(String message) {
    super("SAP系统：" + message);
  }

  public SapException(Throwable cause) {
    super("SAP系统：" + cause.getMessage(), cause);
  }

  public SapException(String message, Throwable cause) {
    super("SAP系统：" + message, cause);
  }

}
