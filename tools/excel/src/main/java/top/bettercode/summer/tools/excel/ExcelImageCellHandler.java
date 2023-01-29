package top.bettercode.summer.tools.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
public class ExcelImageCellHandler<T> implements ExcelCellHandler<T> {

  private static final long serialVersionUID = 1L;

  private final List<ExcelCell<T>> cells = new ArrayList<>();

  @Override
  public void handle(ExcelCell<T> cell) {
    cells.add(cell);
  }

  @Override
  public ExcelCellHandler<T> convert(T from) {
    return this;
  }

  public void setImage(String filename, ExcelConverter<T, byte[]> imageGetter)
      throws IOException {
    setImage(Paths.get(filename).toFile(), imageGetter);
  }

  public void setImage(File file, ExcelConverter<T, byte[]> imageGetter) throws IOException {
    setImage(file, imageGetter, 0);
  }

  public void setImage(File file, ExcelConverter<T, byte[]> imageGetter, int sheetIndex)
      throws IOException {
    XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(file.toPath()));
    XSSFSheet sheet = wb.getSheetAt(sheetIndex);
    CreationHelper helper = wb.getCreationHelper();
    Drawing<XSSFShape> drawing = sheet.createDrawingPatriarch();

    for (ExcelCell<T> cell : cells) {
      if (cell instanceof ExcelRangeCell) {
        ExcelRangeCell<T> rangeCell = (ExcelRangeCell<T>) cell;
        if (rangeCell.needRange()) {
          drawImage(cell, wb, sheet, drawing, helper, imageGetter, cell.getColumn(),
              rangeCell.getLastRangeTop(), rangeCell.getLastRangeBottom() + 1);
          if (rangeCell.isLastRow()) {
            drawImage(cell, wb, sheet, drawing, helper, imageGetter, cell.getColumn(),
                cell.getRow(), cell.getRow() + 1);
          }
        } else if (!rangeCell.isMerge()) {
          drawImage(cell, wb, sheet, drawing, helper, imageGetter, cell.getColumn(), cell.getRow(),
              cell.getRow() + 1);
        }
      } else {
        drawImage(cell, wb, sheet, drawing, helper, imageGetter, cell.getColumn(), cell.getRow(),
            cell.getRow() + 1);
      }
    }

    FileOutputStream outputStream = new FileOutputStream(file);
    wb.write(outputStream);
    wb.close();
    outputStream.close();
  }


  private void drawImage(ExcelCell<T> cell, XSSFWorkbook wb, XSSFSheet sheet,
      Drawing<XSSFShape> drawing, CreationHelper helper, ExcelConverter<T, byte[]> imageGetter,
      int column, int top,
      int bottom) {
    int pictureIdx = wb.addPicture(imageGetter.convert(cell.getEntity()),
        XSSFWorkbook.PICTURE_TYPE_PNG);
    ClientAnchor anchor = helper.createClientAnchor();
    anchor.setCol1(column);
    anchor.setRow1(top);
    anchor.setCol2(column + 1);
    anchor.setRow2(bottom);
    drawing.createPicture(anchor, pictureIdx);

    //set width to n character widths = count characters * 256
    //int widthUnits = 20*256;
    int widthUnits = 20 * 256;
    sheet.setColumnWidth(cell.getColumn(), widthUnits);

    //set height to n points in twips = n * 20
    //short heightUnits = 60*20;
    short heightUnits = 60 * 20;
    sheet.getRow(cell.getRow()).setHeight(heightUnits);
  }

}
