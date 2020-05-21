package cn.bestwu.simpleframework.util.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dhatim.fastexcel.StyleSetter;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 导出Excel文件（导出“XLSX”格式，支持大数据量导出 ）
 */
public class ExcelExport {

  /**
   * 工作薄对象
   */
  private final Workbook workbook;
  /**
   * 工作表对象
   */
  private Worksheet sheet;
  /**
   * 当前行号
   */
  private int r = 0;
  /**
   * 当前单元格号
   */
  private int c = 0;
  /**
   * 是否自动换行
   */
  private boolean wrapText = false;
  /**
   * 是否包含批注
   */
  private boolean includeComment = false;

  /**
   * 是否导出序号
   */
  private boolean serialNumber = false;
  /**
   * 序号名称
   */
  private String serialNumberName = "序号";

  private final ColumnWidths columnWidths = new ColumnWidths();

  /**
   * 构造函数
   *
   * @param os Output stream eventually holding the serialized workbook.
   */
  public ExcelExport(OutputStream os) {
    this(os, null);
  }

  /**
   * 构造函数
   *
   * @param os        Output stream eventually holding the serialized workbook.
   * @param sheetname sheetname
   */
  public ExcelExport(OutputStream os, String sheetname) {
    this.workbook = new Workbook(os, "", "1.0");
    if (StringUtils.hasText(sheetname)) {
      initSheet(sheetname);
    }
  }

  /**
   * @param sheetname sheetname
   * @return this
   */
  public ExcelExport initSheet(String sheetname) {
    this.sheet = workbook.newWorksheet(sheetname);
    setRAndC(0, 0);
    return this;
  }

  public ExcelExport serialNumber(boolean serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

  public ExcelExport serialNumberName(String serialNumberName) {
    this.serialNumberName = serialNumberName;
    return this;
  }

  public ExcelExport setR(int r) {
    this.r = r;
    return this;
  }

  public ExcelExport setC(int c) {
    this.c = c;
    return this;
  }

  public ExcelExport setRAndC(Integer r, Integer c) {
    this.r = r;
    this.c = c;
    return this;
  }

  public ExcelExport includeComment() {
    this.includeComment = true;
    return this;
  }

  public ExcelExport excludeComment() {
    this.includeComment = false;
    return this;
  }

  public Workbook getWorkbook() {
    return workbook;
  }

  public Worksheet getSheet() {
    return sheet;
  }

  public ExcelExport wrapText(boolean wrapText) {
    this.wrapText = wrapText;
    return this;
  }

  public <T> void createHeader(ExcelField<T, ?>[] excelFields) {
    // Create header
    {
      String alignment = Alignment.CENTER.name().toLowerCase();
      if (serialNumber) {
        sheet.value(r, c, serialNumberName);
        columnWidths.put(c, serialNumberName);
        sheet.style(r, c)
            .horizontalAlignment(alignment)
            .verticalAlignment(alignment)
            .bold()
            .fillColor("808080")
            .fontColor("FFFFFF")
            .borderStyle("thin").borderColor("000000")
            .set();
        c++;
      }
      for (ExcelField<T, ?> excelField : excelFields) {
        String t = excelField.title();
        sheet.value(r, c, t);
        double width = excelField.width();
        if (width == -1) {
          columnWidths.put(c, t);
          sheet.width(c, columnWidths.width(c));
        } else {
          sheet.width(c, width);
        }
        sheet.style(r, c)
            .horizontalAlignment(alignment)
            .verticalAlignment(alignment)
            .bold()
            .fillColor("808080")
            .fontColor("FFFFFF")
            .borderStyle("thin").borderColor("000000")
            .set();
        if (includeComment) {
          String commentStr = excelField.comment();
          if (StringUtils.hasText(commentStr)) {
            sheet.comment(r, c, commentStr);
          }
        }
        c++;
      }
    }
    c = 0;
    r++;
  }


  /**
   * @param <T>         E
   * @param list        list
   * @param excelFields 表格字段
   * @return list 数据列表
   */
  public <T> ExcelExport setData(Iterable<T> list, ExcelField<T, ?>[] excelFields) {
    Assert.notNull(sheet, "表格未初始化");
    createHeader(excelFields);
    Iterator<T> iterator = list.iterator();
    while (iterator.hasNext()) {
      T e = iterator.next();
      boolean fill = r % 2 == 0;
      if (serialNumber) {
        sheet.value(r, c, r);
        columnWidths.put(c, r);
        StyleSetter style = sheet.style(r, c)
            .horizontalAlignment(Alignment.CENTER.name().toLowerCase())
            .verticalAlignment(Alignment.CENTER.name().toLowerCase())
            .wrapText(wrapText)
            .borderStyle("thin").borderColor("000000");
        if (fill) {
          style.fillColor("F8F8F7");
        }
        style.set();
        if (!iterator.hasNext()) {
          sheet.width(c, columnWidths.width(c));
        }
        c++;
      }
      for (ExcelField<T, ?> excelField : excelFields) {
        StyleSetter style = sheet.style(r, c)
            .horizontalAlignment(excelField.align().name().toLowerCase())
            .verticalAlignment(Alignment.CENTER.name().toLowerCase())
            .wrapText(wrapText)
            .format(excelField.getCellFormat())
            .borderStyle("thin").borderColor("000000");
        if (fill) {
          style.fillColor("F8F8F7");
        }
        style.set();

        Object val = excelField.toCellValue(e);
        sheet.value(r, c, val);
        if (excelField.width() == -1) {
          columnWidths
              .put(c, val.getClass().equals(Date.class) ? excelField.getCellFormat() : val);

          if (!iterator.hasNext()) {
            sheet.width(c, columnWidths.width(c));
          }
        } else {
          sheet.width(c, excelField.width());
        }
        c++;
      }
      c = 0;
      r++;
    }
    return this;
  }

  public <T> ExcelExport template(ExcelField<T, ?>[] excelFields) throws IOException {
    includeComment = true;
    setData(Collections.emptyList(), excelFields);
    finish();
    return this;
  }

  /**
   * 输出数据流
   *
   * @return this
   * @throws IOException IOException
   */
  public ExcelExport finish() throws IOException {
    workbook.finish();
    return this;
  }

  public <T> ExcelExport template(ExcelField<T, ?>[] excelFields,HttpServletRequest request,
      HttpServletResponse response,
      String fileName) throws IOException {
    write(request, response, fileName);
    return template(excelFields);
  }

  /**
   * 输出数据流
   *
   * @param request  request
   * @param response response
   * @param fileName 输出文件名
   * @return this
   * @throws IOException IOException
   */
  public ExcelExport finish(HttpServletRequest request, HttpServletResponse response,
      String fileName) throws IOException {
    write(request, response, fileName);
    return finish();
  }


  /**
   * 输出到客户端
   *
   * @param request  request
   * @param response response
   * @param fileName 输出文件名
   * @throws IOException IOException
   */
  public static void write(HttpServletRequest request, HttpServletResponse response,
      String fileName) throws IOException {
    response.reset();
    String agent = request.getHeader("USER-AGENT");

    String newFileName;
    if (null != agent && (agent.contains("Trident") || agent.contains("Edge"))) {
      newFileName = URLEncoder.encode(fileName, "UTF-8");
    } else {
      newFileName = MimeUtility.encodeText(fileName, "UTF8", "B");
    }
    response.setHeader("Content-Disposition",
        "attachment;filename=" + newFileName + ".xlsx;filename*=UTF-8''" + URLEncoder
            .encode(fileName, "UTF-8") + ".xlsx");
    response.setContentType("application/vnd.ms-excel; charset=utf-8");
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
  }
}
