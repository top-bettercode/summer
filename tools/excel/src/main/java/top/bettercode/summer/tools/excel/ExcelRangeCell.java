package top.bettercode.summer.tools.excel;

/**
 * @author Peter Wu
 */
public class ExcelRangeCell<T> extends ExcelCell<T> {

  private final boolean mergeLastRange;
  private final int lastRangeTop;
  private final int lastRangeBottom;
  private final Object preCellValue;
  private final boolean needSetValue;
  private final boolean needRange;

  public ExcelRangeCell(int row, int column, int index, int firstRow, boolean lastRow,
      boolean newRange, int lastRangeTop,
      ExcelField<T, ?> excelField, T preEntity, T entity) {
    super(row, column, lastRow, excelField.isMerge() ? index : row - firstRow + 1, index % 2 == 0,
        excelField, entity);

    this.lastRangeTop = lastRangeTop;
    this.lastRangeBottom = lastRow && !newRange ? row : row - 1;
    this.mergeLastRange = (newRange || lastRow) && lastRangeBottom > lastRangeTop;
    this.needSetValue = !getExcelField().isMerge() || newRange;
    this.needRange = mergeLastRange && getExcelField().isMerge();

    if (this.mergeLastRange && !excelField.isIndexColumn()) {
      preCellValue = excelField.toCellValue(preEntity);
    } else {
      preCellValue = null;
    }
  }

  //--------------------------------------------

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
