package cn.bestwu.util.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dhatim.fastexcel.StyleSetter;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

  private final ColumnWidths columnWidths = new ColumnWidths();


  /**
   * @param filename filename eventually holding the serialized workbook .
   * @return ExcelExport
   * @throws FileNotFoundException FileNotFoundException
   */
  public static ExcelExport of(String filename) throws FileNotFoundException {
    return new ExcelExport(new FileOutputStream(filename));
  }

  /**
   * @param file filename eventually holding the serialized workbook .
   * @return ExcelExport
   * @throws FileNotFoundException FileNotFoundException
   */
  public static ExcelExport of(File file) throws FileNotFoundException {
    File parentFile = file.getParentFile();
    if (!parentFile.exists()) {
      parentFile.mkdirs();
    }
    return new ExcelExport(new FileOutputStream(file));
  }


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
        setHeaderStyle();
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

  private void setHeaderStyle() {
    sheet.style(r, c)
        .horizontalAlignment(Alignment.center.name())
        .verticalAlignment(Alignment.center.name())
        .bold()
        .fillColor("808080")
        .fontColor("FFFFFF")
        .borderStyle("thin").borderColor("000000")
        .set();
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
    Assert.notNull(sheet, "表格未初始化");
    createHeader(excelFields);
    Iterator<T> iterator = list.iterator();
    int firstRow = r;
    int firstColumn = c;
    while (iterator.hasNext()) {
      T e = converter.convert(iterator.next());
      boolean lastRow = !iterator.hasNext();
      for (ExcelField<T, ?> excelField : excelFields) {
        setCell(new ExcelCell(r, c, firstRow, lastRow, excelField, e));
        c++;
      }
      c = firstColumn;
      r++;
    }
    return this;
  }

  private void setCell(ExcelCell excelCell) {
    int column = excelCell.getColumn();
    int row = excelCell.getRow();
    StyleSetter style = sheet.style(row, column);
    String pattern = excelCell.getPattern();
    style.horizontalAlignment(excelCell.getAlign())
        .verticalAlignment(Alignment.center.name())
        .wrapText(wrapText)
        .format(pattern)
        .borderStyle("thin")
        .borderColor("000000");

    if (excelCell.isFillColor()) {
      style.fillColor("F8F8F7");
    }
    style.set();
    Object cellValue = excelCell.getCellValue();
    sheet.value(row, column, cellValue);
    double width = excelCell.getWidth();
    if (width == -1) {
      columnWidths.put(column, excelCell.isDateField() ? pattern : cellValue);
      if (excelCell.isLastRow()) {
        sheet.width(column, columnWidths.width(column));
      }
    } else {
      sheet.width(column, width);
    }
  }

  /**
   * @param <T>         E
   * @param list        list
   * @param excelFields 表格字段
   * @return list 数据列表
   */
  public <T> ExcelExport setMergeData(Iterable<T> list, ExcelField<T, ?>[] excelFields) {
    return setMergeData(list, excelFields, (o) -> o);
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
  public <T> ExcelExport setMergeData(Iterable<T> list, ExcelField<T, ?>[] excelFields,
      ExcelConverter<T, T> converter) {
    Assert.notNull(sheet, "表格未初始化");
    createHeader(excelFields);
    Iterator<T> iterator = list.iterator();
    int firstRow = r;
    int firstColumn = c;

    int index = 0;
    int mergeIndex = 0;

    Map<Integer, Object> lastMergeIds = new HashMap<>();
    Map<Integer, Integer> lastRangeTops = new HashMap<>();
    while (iterator.hasNext()) {
      T e = converter.convert(iterator.next());
      boolean lastRow = !iterator.hasNext();

      int lastRowRangeTop = firstRow;

      List<ExcelRangeCell> cells = new ArrayList<>();
      for (ExcelField<T, ?> excelField : excelFields) {
        if (excelField.isMerge()) {
          Object mergeIdValue = excelField.mergeId(e);
          Object lastMergeId = lastMergeIds.get(mergeIndex);
          boolean newRange = lastMergeId == null || !lastMergeId.equals(mergeIdValue);
          if (newRange) {
            lastMergeIds.put(mergeIndex, mergeIdValue);
          }

          int lastRangeTop = lastRangeTops.getOrDefault(mergeIndex, firstRow);

          if (mergeIndex == 0) {
            if (newRange) {
              index++;
            }
            lastRowRangeTop = lastRangeTop;
          }
          if (lastRangeTops.getOrDefault(0, firstRow) == r) {
            newRange = true;
          }

          cells
              .add(new ExcelRangeCell(r, c, index, firstRow, lastRow, excelField, e, newRange,
                  Math.max(lastRangeTop, lastRowRangeTop)));
          if (newRange) {
            lastRangeTops.put(mergeIndex, r);
          }
          mergeIndex++;
        } else {
          cells.add(
              new ExcelRangeCell(r, c, index, firstRow, lastRow, excelField, e, false, firstRow));
        }
        c++;
      }
      for (ExcelRangeCell cell : cells) {
        cell.setIndex(index);
        setRangeCell(cell);
      }
      mergeIndex = 0;
      c = firstColumn;
      r++;
    }
    return this;
  }

  private void setRangeCell(ExcelRangeCell excelCell) {
    int column = excelCell.getColumn();
    if (excelCell.needSetValue()) {
      setCell(excelCell);
    } else {
      sheet.value(excelCell.getRow(), column);
    }

    if (excelCell.needRange()) {
      sheet.range(excelCell.getLastRangeTop(), column, excelCell.getLastRangeBottom(), column)
          .merge();
      if (excelCell.isLastRow()) {
        double width = excelCell.getWidth();
        String pattern = excelCell.getPattern();
        if (width == -1) {
          sheet.width(column, columnWidths.width(column));
        } else {
          sheet.width(column, width);
        }
        StyleSetter style = sheet
            .range(excelCell.getLastRangeTop(), column, excelCell.getLastRangeBottom(), column)
            .style();
        style.horizontalAlignment(excelCell.getAlign())
            .verticalAlignment(Alignment.center.name())
            .wrapText(wrapText)
            .format(pattern)
            .borderStyle("thin")
            .borderColor("000000");

        if (excelCell.isFillColor()) {
          style.fillColor("F8F8F7");
        }
        style.set();
      }
    }
  }


  public <T> ExcelExport template(ExcelField<T, ?>[] excelFields) {
    includeComment = true;
    setData(Collections.emptyList(), excelFields);
    return this;
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
   * @param fileName 输出文件名
   * @param consumer 处理生成excel
   * @throws IOException IOException
   */
  public static void export(String fileName, Consumer<ExcelExport> consumer) throws IOException {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    Assert.notNull(requestAttributes, "requestAttributes获取失败");
    HttpServletRequest request = requestAttributes.getRequest();
    HttpServletResponse response = requestAttributes.getResponse();
    export(request, response, fileName, consumer);
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
  public static void export(HttpServletRequest request, HttpServletResponse response,
      String fileName, Consumer<ExcelExport> consumer) throws IOException {
    setResponseHeader(request, response, fileName);
    ExcelExport excelExport = ExcelExport.of(response.getOutputStream());
    consumer.accept(excelExport);
    excelExport.finish();
  }

  /**
   * 文件缓存输出
   *
   * @param fileName 输出文件名
   * @param fileKey  文件唯一key
   * @param consumer 处理生成excel
   * @throws IOException IOException
   */
  public static void cache(String fileName, String fileKey, Consumer<ExcelExport> consumer)
      throws IOException {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    Assert.notNull(requestAttributes, "requestAttributes获取失败");
    HttpServletRequest request = requestAttributes.getRequest();
    HttpServletResponse response = requestAttributes.getResponse();
    cache(request, response, fileName, fileKey, consumer);
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
  public static void cache(HttpServletRequest request, HttpServletResponse response,
      String fileName, String fileKey, Consumer<ExcelExport> consumer) throws IOException {
    cacheOutput(request, response, fileName, fileKey, outputStream -> {
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
   * @param fileName 输出文件名
   * @param fileKey  文件唯一key
   * @param consumer 处理生成excel至 outputStream
   * @throws IOException IOException
   */
  public static void cacheOutput(String fileName, String fileKey, Consumer<OutputStream> consumer)
      throws IOException {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    Assert.notNull(requestAttributes, "requestAttributes获取失败");
    HttpServletRequest request = requestAttributes.getRequest();
    HttpServletResponse response = requestAttributes.getResponse();
    cacheOutput(request, response, fileName, fileKey, consumer);
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
  public static void cacheOutput(HttpServletRequest request, HttpServletResponse response,
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
