package top.bettercode.summer.tools.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author Peter Wu
 */
public class ExcelImageCellWriterUtil {


  public static void setImage(String sheetName, List<ExcelCell<?>> imageCells,
      InputStream inputStream, OutputStream outputStream) {
    try {
      XSSFWorkbook wb = new XSSFWorkbook(inputStream);
      XSSFSheet sheet = wb.getSheet(sheetName);
      CreationHelper helper = wb.getCreationHelper();
      Drawing<XSSFShape> drawing = sheet.createDrawingPatriarch();

      for (ExcelCell<?> cell : imageCells) {
        if (cell instanceof ExcelRangeCell && cell.getExcelField().isMerge()) {
          ExcelRangeCell<?> rangeCell = (ExcelRangeCell<?>) cell;
          if (((ExcelRangeCell<?>) cell).newRange()) {
            drawImage(((ExcelRangeCell<?>) cell).getPreCellValue(), wb, sheet, drawing, helper,
                cell.getColumn(),
                rangeCell.getLastRangeTop(),
                rangeCell.getLastRangeTop() + 1);
            if (rangeCell.isLastRow()) {
              drawImage(cell.getCellValue(), wb, sheet, drawing, helper,
                  cell.getColumn(),
                  cell.getRow(), cell.getRow() + 1);
            }
          } else if (rangeCell.isLastRow()) {
            drawImage(cell.getCellValue(), wb, sheet, drawing, helper,
                cell.getColumn(),
                rangeCell.getLastRangeTop(), rangeCell.getLastRangeTop() + 1);
          }
        } else {
          drawImage(cell.getCellValue(), wb, sheet, drawing, helper,
              cell.getColumn(),
              cell.getRow(),
              cell.getRow() + 1);
        }
      }

      wb.write(outputStream);
      wb.close();
      outputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private static void drawImage(Object cellValue, XSSFWorkbook wb, XSSFSheet sheet,
      Drawing<XSSFShape> drawing, CreationHelper helper, int column, int top, int bottom)
      throws IOException {
    if (cellValue == null || "".equals(cellValue)) {
      return;
    }
    int pictureIdx;
    if (cellValue instanceof byte[]) {
      pictureIdx = wb.addPicture((byte[]) cellValue, XSSFWorkbook.PICTURE_TYPE_PNG);
    } else if (cellValue instanceof InputStream) {
      pictureIdx = wb.addPicture((InputStream) cellValue, XSSFWorkbook.PICTURE_TYPE_PNG);
    } else {
      throw new ExcelException(
          "图像单元格数据:" + cellValue + "不是有效输入格式（byte[] or InputStream）");
    }
    ClientAnchor anchor = helper.createClientAnchor();
    anchor.setCol1(column);
    anchor.setRow1(top);
    anchor.setCol2(column + 1);
    anchor.setRow2(bottom);
    drawing.createPicture(anchor, pictureIdx);
  }

}
