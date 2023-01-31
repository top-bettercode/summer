package top.bettercode.summer.tools.excel;

/**
 * @author Peter Wu
 */
public class ExcelCell<T> {

  /**
   * 默认日期格式
   */
  public static final String DEFAULT_DATE_FORMAT = "yyyy-m-dd";
  /**
   * 默认时间格式
   */
  public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-m-dd hh:mm";
  /**
   * 默认格式
   */
  public static final String DEFAULT_FORMAT = "@";

  private final int row;
  private final int column;
  private final boolean lastRow;
  private boolean fillColor;
  private final Object cellValue;
  private final T entity;
  private final ExcelField<T, ?> excelField;

  public ExcelCell(int row, int column, int firstRow, boolean lastRow,
      ExcelField<T, ?> excelField, T entity) {
    this(row, column, lastRow, row - firstRow + 1, (row - firstRow + 1) % 2 == 0, excelField,
        entity);
  }

  public ExcelCell(int row, int column, boolean lastRow, int index, boolean fillColor,
      ExcelField<T, ?> excelField, T entity) {
    this.row = row;
    this.column = column;
    this.lastRow = lastRow;
    this.fillColor = fillColor;
    this.excelField = excelField;
    this.entity = entity;

    if (this.excelField.isIndexColumn()) {
      this.cellValue = index;
    } else {
      this.cellValue = excelField.toCellValue(entity);
    }
  }

  public void setFillColor(int index) {
    this.fillColor = index % 2 == 0;
  }

  public boolean needSetValue() {
    return true;
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }

  public boolean isLastRow() {
    return lastRow;
  }


  public boolean isFillColor() {
    return fillColor;
  }

  public Object getCellValue() {
    return cellValue;
  }

  public T getEntity() {
    return entity;
  }

  public ExcelField<T, ?> getExcelField() {
    return excelField;
  }
}
