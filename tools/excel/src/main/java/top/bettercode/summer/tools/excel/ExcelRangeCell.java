package top.bettercode.summer.tools.excel;

/**
 * @author Peter Wu
 */
public class ExcelRangeCell<T> extends ExcelCell<T> {

  private final int lastRangeTop;
  private final int lastRangeBottom;
  private final Object preCellValue;
  private final boolean needSetValue;
  private final boolean needRange;
  private final boolean newRange;

  public ExcelRangeCell(int row, int column, int index, int firstRow, boolean lastRow,
      boolean newRange, int lastRangeTop,
      ExcelField<T, ?> excelField, T preEntity, T entity) {
    super(row, column, lastRow, excelField.isMerge() ? index : row - firstRow + 1, index % 2 == 0,
        excelField, entity);

    this.newRange = newRange;
    this.lastRangeTop = lastRangeTop;

    this.lastRangeBottom = newRange && index > 1 ? row - 1 : row;
    boolean mergeLastRange = (newRange || lastRow) && lastRangeBottom > lastRangeTop;
    this.needRange = mergeLastRange && getExcelField().isMerge();
    this.needSetValue = !getExcelField().isMerge() || newRange;

    if (!excelField.isIndexColumn() && newRange && preEntity != null) {
      preCellValue = excelField.toCellValue(preEntity);
    } else {
      preCellValue = null;
    }
  }

  //--------------------------------------------


  public boolean newRange() {
    return newRange;
  }

  public boolean needSetValue() {
    return needSetValue;
  }

  public boolean needRange() {
    return needRange;
  }

  //--------------------------------------------

  public Object getPreCellValue() {
    return preCellValue;
  }

  public int getLastRangeTop() {
    return lastRangeTop;
  }

  public int getLastRangeBottom() {
    return lastRangeBottom;
  }

}
