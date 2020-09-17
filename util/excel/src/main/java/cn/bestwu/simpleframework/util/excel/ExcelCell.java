package cn.bestwu.simpleframework.util.excel;

/**
 * @author Peter Wu
 */
public class ExcelCell {

  /**
   * 默认时间格式
   */
  public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm";
  /**
   * 默认格式
   */
  public static final String DEFAULT_PATTERN = "@";

  private int row;
  private int column;

  private boolean lastRow;
  private boolean fillColor;
  private String align;
  private double width;
  private String pattern;
  private Object cellValue;

  public <T> ExcelCell(int row, int column, boolean lastRow,
      ExcelField<T, ?> excelField, T entity) {
    this(row, column, excelField.index() % 2 == 0, lastRow, excelField, entity);
  }

  public <T> ExcelCell(int row, int column, boolean fillColor, boolean lastRow,
      ExcelField<T, ?> excelField, T entity) {
    this.row = row;
    this.column = column;
    this.lastRow = lastRow;
    this.fillColor = fillColor;
    this.align = excelField.align().name();
    this.width = excelField.width();
    this.pattern = excelField.pattern();
    if (excelField.isIndexColumn()) {
      this.cellValue = excelField.index();
    } else {
      this.cellValue = excelField.toCellValue(entity);
    }
  }

  public int getRow() {
    return row;
  }

  public void setRow(int row) {
    this.row = row;
  }

  public int getColumn() {
    return column;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  public boolean isLastRow() {
    return lastRow;
  }

  public void setLastRow(boolean lastRow) {
    this.lastRow = lastRow;
  }

  public boolean isFillColor() {
    return fillColor;
  }

  public void setFillColor(boolean fillColor) {
    this.fillColor = fillColor;
  }

  public String getAlign() {
    return align;
  }

  public void setAlign(String align) {
    this.align = align;
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public Object getCellValue() {
    return cellValue;
  }

  public void setCellValue(Object cellValue) {
    this.cellValue = cellValue;
  }

}
