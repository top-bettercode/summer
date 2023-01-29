package top.bettercode.summer.tools.excel;

/**
 * @author Peter Wu
 */
public interface ExcelCellHandler<T> extends ExcelConverter<T, ExcelCellHandler<T>> {

  void handle(ExcelCell<T> cell);

}
