package cn.bestwu.simpleframework.util.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Consumer;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dhatim.fastexcel.BorderSide;
import org.dhatim.fastexcel.StyleSetter;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
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
   * @param os Output stream eventually holding the serialized workbook.
   * @return ExcelExport
   */
  public static ExcelExport of(OutputStream os) {
    return new ExcelExport(os);
  }

  /**
   * 构造函数
   *
   * @param os Output stream eventually holding the serialized workbook.
   */
  private ExcelExport(OutputStream os) {
    this.workbook = new Workbook(os, "", "1.0");
  }

  /**
   * @param sheetname sheetname
   * @return this
   */
  public ExcelExport sheet(String sheetname) {
    this.sheet = workbook.newWorksheet(sheetname);
    setRowAndColumn(0, 0);
    return this;
  }

  public ExcelExport serialNumber() {
    this.serialNumber = true;
    return this;
  }

  public ExcelExport serialNumber(boolean serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

  public ExcelExport serialNumberName(String serialNumberName) {
    this.serialNumber = true;
    this.serialNumberName = serialNumberName;
    return this;
  }

  /**
   * @param row 行号，从0开始
   * @return this ExcelExport
   */
  public ExcelExport setRow(int row) {
    this.r = row;
    return this;
  }

  /**
   * @param column 列号，从0开始
   * @return this ExcelExport
   */
  public ExcelExport setColumn(int column) {
    this.c = column;
    return this;
  }

  /**
   * @param row    行号，从0开始
   * @param column 列号，从0开始
   * @return this ExcelExport
   */
  public ExcelExport setRowAndColumn(Integer row, Integer column) {
    this.r = row;
    this.c = column;
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
      String alignment = Alignment.center.name();
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
        if (excelField.isMergeId()) {
          continue;
        }
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
    return setData(list, excelFields, (o) -> o);
  }

  /**
   * @param <T>         E
   * @param list        list
   * @param excelFields 表格字段
   * @param converter   转换器
   * @return list 数据列表
   */
  public <T> ExcelExport setData(Iterable<T> list, ExcelField<T, ?>[] excelFields,
      ExcelConverter<T, T> converter) {
    if (excelFields[0].isMergeId()) {
      return setMergeData(list, excelFields, converter);
    }
    Assert.notNull(sheet, "表格未初始化");
    createHeader(excelFields);
    Iterator<T> iterator = list.iterator();
    while (iterator.hasNext()) {
      T e = converter.convert(iterator.next());
      boolean hasNext = iterator.hasNext();
      boolean fill = r % 2 == 0;
      if (serialNumber) {
        sheet.value(r, c, r);
        columnWidths.put(c, r);
        StyleSetter style = sheet.style(r, c)
            .horizontalAlignment(Alignment.center.name())
            .verticalAlignment(Alignment.center.name())
            .wrapText(wrapText)
            .borderStyle("thin").borderColor("000000");
        if (fill) {
          style.fillColor("F8F8F7");
        }
        style.set();
        if (!hasNext) {
          sheet.width(c, columnWidths.width(c));
        }
        c++;
      }
      for (ExcelField<T, ?> excelField : excelFields) {
        StyleSetter style = sheet.style(r, c)
            .horizontalAlignment(excelField.align().name())
            .verticalAlignment(Alignment.center.name())
            .wrapText(wrapText)
            .format(excelField.pattern())
            .borderStyle("thin").borderColor("000000");

        setCellValue(hasNext, fill, style, excelField.toCellValue(e), excelField.width(),
            excelField.pattern());
        c++;
      }
      c = 0;
      r++;
    }
    return this;
  }

  private void setCellValue(boolean hasNext, boolean fill, StyleSetter style, Object cellValue,
      double width, String pattern) {
    if (fill) {
      style.fillColor("F8F8F7");
    }
    style.set();

    sheet.value(r, c, cellValue);
    if (width == -1) {
      columnWidths
          .put(c, cellValue.getClass().equals(Date.class) ? pattern : cellValue);

      if (!hasNext) {
        sheet.width(c, columnWidths.width(c));
      }
    } else {
      sheet.width(c, width);
    }
  }

  /**
   * 用于导出有合并行的Execel，第一个ExcelField为mergeId列，此列不导出，用于判断是否合并之前相同mergeId的行
   *
   * @param <T>         E
   * @param list        list
   * @param excelFields 表格字段
   * @param converter   转换器
   * @return list 数据列表
   */
  private <T> ExcelExport setMergeData(Iterable<T> list, ExcelField<T, ?>[] excelFields,
      ExcelConverter<T, T> converter) {
    Assert.notNull(sheet, "表格未初始化");
    createHeader(excelFields);
    Iterator<T> iterator = list.iterator();
    Object mergeId = null;
    boolean fill = true;
    int top = r;
    int no = 1;
    ExcelField<T, ?> mergeField = excelFields[0];
    int right = excelFields.length - 2;
    if (serialNumber) {
      right++;
    }
    while (iterator.hasNext()) {
      T e = converter.convert(iterator.next());

      boolean hasNext = iterator.hasNext();
      Object mergeIdValue = mergeField.toCellValue(e);
      boolean newItem = mergeId == null || !mergeId.equals(mergeIdValue);
      if (newItem) {
        fill = !fill;
        mergeId = mergeIdValue;
      }
      int bottom = hasNext ? r - 1 : r;
      boolean merge = (newItem || !hasNext) && bottom > top;
      if (serialNumber) {
        if (merge) {
          sheet.range(top, c, bottom, c).merge();
          sheet.range(top, c, bottom, c).style().horizontalAlignment("center")
              .verticalAlignment("center").set();
        }
        if (newItem) {
          sheet.value(r, c, no++);
          columnWidths.put(c, no);
          StyleSetter style = sheet.style(r, c)
              .horizontalAlignment(Alignment.center.name())
              .verticalAlignment(Alignment.center.name())
              .wrapText(wrapText);
          if (fill) {
            style.fillColor("F8F8F7");
          }
          style.set();
          if (!hasNext) {
            sheet.width(c, columnWidths.width(c));
          }
        } else {
          sheet.value(r, c);
        }
        c++;
      }

      for (int i = 1; i < excelFields.length; i++) {
        ExcelField<T, ?> excelField = excelFields[i];
        if (merge && excelField.isMerge()) {
          sheet.range(top, c, bottom, c).merge();
          sheet.width(c, columnWidths.width(c));
          sheet.range(top, c, bottom, c).style().horizontalAlignment("center")
              .verticalAlignment("center").set();
        }
        if (excelField.isMerge() && !newItem) {
          sheet.value(r, c);
        } else {
          StyleSetter style = sheet.style(r, c)
              .horizontalAlignment(excelField.align().name())
              .verticalAlignment(Alignment.center.name())
              .wrapText(wrapText)
              .format(excelField.pattern());
          setCellValue(hasNext, fill, style, excelField.toCellValue(e), excelField.width(),
              excelField.pattern());
        }
        c++;
      }
      if (merge) {
        sheet.range(top, 0, top, right).style()
            .borderStyle(BorderSide.TOP, "thin")
            .borderColor(BorderSide.TOP, "000000")
            .set();
        sheet.range(bottom, 0, bottom, right).style()
            .borderStyle(BorderSide.BOTTOM, "thin")
            .borderColor(BorderSide.BOTTOM, "000000")
            .set();
        sheet.range(top, 0, top, 0).style()
            .borderStyle(BorderSide.TOP, "thin")
            .borderColor(BorderSide.TOP, "000000")
            .borderStyle(BorderSide.LEFT, "thin")
            .borderColor(BorderSide.LEFT, "000000")
            .set();
        sheet.range(bottom, 0, bottom, 0).style()
            .borderStyle(BorderSide.BOTTOM, "thin")
            .borderColor(BorderSide.BOTTOM, "000000")
            .borderStyle(BorderSide.LEFT, "thin")
            .borderColor(BorderSide.LEFT, "000000")
            .set();
        sheet.range(top, right, top, right).style()
            .borderStyle(BorderSide.TOP, "thin")
            .borderColor(BorderSide.TOP, "000000")
            .borderStyle(BorderSide.RIGHT, "thin")
            .borderColor(BorderSide.RIGHT, "000000")
            .set();
        sheet.range(bottom, right, bottom, right).style()
            .borderStyle(BorderSide.BOTTOM, "thin")
            .borderColor(BorderSide.BOTTOM, "000000")
            .borderStyle(BorderSide.RIGHT, "thin")
            .borderColor(BorderSide.RIGHT, "000000")
            .set();

        top++;
        bottom--;
        if (bottom >= top) {
          sheet.range(top, 0, bottom, 0).style()
              .borderStyle(BorderSide.LEFT, "thin")
              .borderColor(BorderSide.LEFT, "000000")
              .set();
          sheet.range(top, right, bottom, right).style()
              .borderStyle(BorderSide.RIGHT, "thin")
              .borderColor(BorderSide.RIGHT, "000000")
              .set();
        }

        top = r;
      }
      c = 0;
      r++;
    }
    return this;
  }


  public <T> void template(ExcelField<T, ?>[] excelFields) throws IOException {
    includeComment = true;
    setData(Collections.emptyList(), excelFields);
    finish();
  }

  /**
   * 输出数据流
   *
   * @throws IOException IOException
   */
  public void finish() throws IOException {
    workbook.finish();
  }

  /**
   * 输出数据流
   *
   * @param request  request
   * @param response response
   * @param fileName 输出文件名
   * @param consumer 处理生成excel
   * @throws IOException IOException
   */
  public static void write(HttpServletRequest request, HttpServletResponse response,
      String fileName, Consumer<ExcelExport> consumer) throws IOException {
    setResponseHeader(request, response, fileName);
    ExcelExport excelExport = ExcelExport.of(response.getOutputStream());
    consumer.accept(excelExport);
    excelExport.finish();
  }

  /**
   * 文件缓存输出
   *
   * @param request  request
   * @param response response
   * @param fileName 输出文件名
   * @param fileKey  文件唯一key
   * @param consumer 处理生成excel
   * @throws IOException IOException
   */
  public static void writeCache(HttpServletRequest request, HttpServletResponse response,
      String fileName, String fileKey, Consumer<ExcelExport> consumer) throws IOException {
    writeCache1(request, response, fileName, fileKey, outputStream -> {
      try {
        ExcelExport excelExport = ExcelExport.of(outputStream);
        consumer.accept(excelExport);
        excelExport.finish();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * 文件缓存输出
   *
   * @param request  request
   * @param response response
   * @param fileName 输出文件名
   * @param fileKey  文件唯一key
   * @param consumer 处理生成excel至 outputStream
   * @throws IOException IOException
   */
  public static void writeCache1(HttpServletRequest request, HttpServletResponse response,
      String fileName, String fileKey, Consumer<OutputStream> consumer) throws IOException {
    setResponseHeader(request, response, fileName);
    String tmpPath = System.getProperty("java.io.tmpdir");

    File file = new File(tmpPath,
        "excel-export" + File.separator + fileName + File.separator + fileKey + ".xlsx");
    if (!file.exists()) {
      File dir = file.getParentFile();
      if (!dir.exists()) {
        dir.mkdirs();
      }
      File tmpFile = new File(file + "-" + UUID.randomUUID().toString());
      try (OutputStream outputStream = new FileOutputStream(tmpFile)) {
        consumer.accept(outputStream);
      }
      tmpFile.renameTo(file);
    }
    StreamUtils.copy(new FileInputStream(file), response.getOutputStream());
  }

  /**
   * 输出到客户端
   *
   * @param request  request
   * @param response response
   * @param fileName 输出文件名
   * @throws IOException IOException
   */
  private static void setResponseHeader(HttpServletRequest request, HttpServletResponse response,
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
