package cn.bestwu.simpleframework.util.excel;

import java.util.List;

/**
 * @author Peter Wu
 */
public class ExcelImportException extends Exception {

  private static final long serialVersionUID = 1L;

  private final List<CellError> errors;


  public ExcelImportException(String message,
      List<CellError> errors, Throwable e) {
    super(message, e);
    this.errors = errors;
  }

  public List<CellError> getErrors() {
    return errors;
  }

  public static class CellError {

    private String message = "excel.cell.typeMismatch";
    private final Integer row;
    private final Integer column;
    private final String title;
    private final String value;
    private final Exception exception;

    /**
     * @param row       行号
     * @param column    列号
     * @param title     表格列名
     * @param value     表格单元格值
     * @param exception 异常
     */
    public CellError(Integer row, Integer column, String title, String value,
        Exception exception) {
      this.row = row;
      this.column = column;
      this.title = title;
      this.value = value;
      this.exception = exception;
    }

    public String getMessage() {
      return message;
    }

    public CellError setMessage(String message) {
      this.message = message;
      return this;
    }

    public String getColumnName() {
      int i = column;
      StringBuilder chars = new StringBuilder();
      do {
        chars.append((char) ('A' + i % 26));
      } while ((i = ((i / 26) - 1)) >= 0);
      return chars.reverse().toString();
    }

    public Integer getRow() {
      return row;
    }

    public Integer getColumn() {
      return column;
    }

    public Exception getException() {
      return exception;
    }

    public String getTitle() {
      return title;
    }

    public String getValue() {
      return value;
    }
  }
}
