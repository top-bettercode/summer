package cn.bestwu.simpleframework.util.excel;

/**
 * @author Peter Wu
 */
public class ExcelRangeCell extends ExcelCell {

  private boolean newRange;
  private boolean mergeLastRange;
  private boolean merge;
  private int lastRangeTop;
  private int lastRangeBottom;
  private int firstColumn;
  private int lastColumn;

  public <T> ExcelRangeCell(int row, int column, boolean lastRow, boolean fillColor,
      ExcelField<T, ?> excelField, T entity, boolean newRange,
      int lastRangeTop, int firstColumn,
      int lastColumn) {
    super(row, column, fillColor, lastRow, excelField, entity);

    this.newRange = newRange;
    this.lastRangeBottom = lastRow ? row : row - 1;
    this.mergeLastRange = (newRange || lastRow) && lastRangeBottom > lastRangeTop;
    this.merge = excelField.isMerge();
    this.lastRangeTop = lastRangeTop;
    this.firstColumn = firstColumn;
    this.lastColumn = lastColumn;
  }

  //--------------------------------------------

  public boolean needSetValue() {
    return !merge || newRange;
  }

  public boolean needRange() {
    return mergeLastRange && merge;
  }

  //--------------------------------------------

  public boolean isNewRange() {
    return newRange;
  }

  public void setNewRange(boolean newRange) {
    this.newRange = newRange;
  }

  public boolean isMergeLastRange() {
    return mergeLastRange;
  }

  public void setMergeLastRange(boolean mergeLastRange) {
    this.mergeLastRange = mergeLastRange;
  }

  public boolean isMerge() {
    return merge;
  }

  public void setMerge(boolean merge) {
    this.merge = merge;
  }

  public int getLastRangeTop() {
    return lastRangeTop;
  }

  public void setLastRangeTop(int lastRangeTop) {
    this.lastRangeTop = lastRangeTop;
  }

  public int getLastRangeBottom() {
    return lastRangeBottom;
  }

  public void setLastRangeBottom(int lastRangeBottom) {
    this.lastRangeBottom = lastRangeBottom;
  }

  public int getFirstColumn() {
    return firstColumn;
  }

  public void setFirstColumn(int firstColumn) {
    this.firstColumn = firstColumn;
  }

  public int getLastColumn() {
    return lastColumn;
  }

  public void setLastColumn(int lastColumn) {
    this.lastColumn = lastColumn;
  }
}
