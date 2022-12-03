package top.bettercode.summer.tools.excel;

/**
 * @author Peter Wu
 */
public class ExcelCell {

  /**
   * 默认日期格式
   */
  public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
  /**
   * 默认时间格式
   */
  public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";
  /**
   * 默认日期格式
   */
  public static final String DEFAULT_DATE_NUMBERING_FORMAT = "yyyy-m-dd";
  /**
   * 默认时间格式
   */
  public static final String DEFAULT_DATE_TIME_NUMBERING_FORMAT = "yyyy-m-dd hh:mm";
  /**
   * 默认格式
   */
  public static final String DEFAULT_NUMBERING_FORMAT = "@";

  private final int row;
  private final int column;

  private final boolean lastRow;
  protected boolean fillColor;
  private final String align;
  private final double width;
  private final String numberingFormat;
  protected Object cellValue;
  protected final boolean indexColumn;
  private final boolean dateField;

  public <T> ExcelCell(int row, int column, int firstRow, boolean lastRow,
      ExcelField<T, ?> excelField, T entity) {
    this(row, column, lastRow, row - firstRow + 1, excelField, entity);
  }

  public <T> ExcelCell(int row, int column, boolean lastRow, int index,
      ExcelField<T, ?> excelField, T entity) {
    this.row = row;
    this.column = column;
    this.lastRow = lastRow;
    this.dateField = excelField.isDateField();
    this.fillColor = index % 2 == 0;
    this.align = excelField.align().name();
    this.width = excelField.width();
    this.numberingFormat = excelField.numberingFormat();
    this.indexColumn = excelField.isIndexColumn();
    if (this.indexColumn) {
      this.cellValue = index;
    } else {
      this.cellValue = excelField.toCellValue(entity);
    }
  }

  public boolean needSetValue() {
    return true;
  }

  public void setIndex(int index) {
    this.fillColor = index % 2 == 0;
    if (indexColumn) {
      this.cellValue = index;
    }
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

  public boolean isDateField() {
    return dateField;
  }

  public boolean isFillColor() {
    return fillColor;
  }

  public String getAlign() {
    return align;
  }

  public double getWidth() {
    return width;
  }

  public String getNumberingFormat() {
    return numberingFormat;
  }

  public Object getCellValue() {
    return cellValue;
  }
}
