package cn.bestwu.simpleframework.util.excel;

import java.util.List;

/**
 * @author Peter Wu
 */
public class ExcelImportException extends Exception {

  private static final long serialVersionUID = 1L;

  private final List<CellError> errors;

  public ExcelImportException(List<CellError> errors) {
    this(null, errors);
  }

  public ExcelImportException(String message,
      List<CellError> errors) {
    super(message);
    this.errors = errors;
  }

  public List<CellError> getErrors() {
    return errors;
  }

  public static class CellError {

    private Integer row;
    private Integer column;
    private String title;
    private String value;
    private Exception exception;

    /**
     * @param row 行号
     * @param column 列号
     * @param title 表格列名
     * @param value 表格单元格值
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