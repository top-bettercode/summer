package top.bettercode.summer.tools.excel;

/**
 * @author Peter Wu
 */
public class ExcelRangeCell extends ExcelCell {

  private final boolean newRange;
  private final boolean mergeLastRange;
  private final boolean merge;
  private final int lastRangeTop;
  private final int lastRangeBottom;
  private final int firstRow;

  public <T> ExcelRangeCell(int row, int column, int index, int firstRow, boolean lastRow,
      ExcelField<T, ?> excelField, T entity,
      boolean newRange, int lastRangeTop) {
    super(row, column, lastRow, index, excelField, entity);

    this.firstRow = firstRow;
    this.newRange = newRange;
    this.lastRangeBottom = lastRow && !newRange ? row : row - 1;
    this.mergeLastRange = (newRange || lastRow) && lastRangeBottom > lastRangeTop;
    this.merge = excelField.isMerge();
    this.lastRangeTop = lastRangeTop;
  }

  //--------------------------------------------


  @Override
  public void setIndex(int index) {
    fillColor = index % 2 == 0;
    if (indexColumn) {
      cellValue = merge ? index : (getRow() - firstRow + 1);
    }
  }

  public boolean needSetValue() {
    return !merge || newRange;
  }

  public boolean needRange() {
    return mergeLastRange && merge;
  }

  //--------------------------------------------

  public int getLastRangeTop() {
    return lastRangeTop;
  }

  public int getLastRangeBottom() {
    return lastRangeBottom;
  }

}
