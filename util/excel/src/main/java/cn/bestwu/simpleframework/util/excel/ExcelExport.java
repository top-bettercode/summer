package cn.bestwu.simpleframework.util.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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

  /**
   * 是否导出序号
   */
  private boolean serialNumber = false;
  /**
   * 序号名称
   */
  private String serialNumberName = "序号";

  private final ColumnWidths columnWidths = new ColumnWidths();

  private final BorderSide[] defaultBorderSides = new BorderSide[]{BorderSide.TOP, BorderSide.LEFT,
      BorderSide.BOTTOM,
      BorderSide.RIGHT};

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
      if (serialNumber) {
        sheet.value(r, c, serialNumberName);
        columnWidths.put(c, serialNumberName);
        sheet.width(c, columnWidths.width(c));
        setHeaderStyle();
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
    if (excelFields[0].isMergeId()) {
      return setMergeData(list, excelFields, converter);
    }
    Assert.notNull(sheet, "表格未初始化");
    createHeader(excelFields);
    Iterator<T> iterator = list.iterator();
    int firstColumn = c;

    while (iterator.hasNext()) {
      T e = converter.convert(iterator.next());
      boolean hasNext = iterator.hasNext();
      boolean fill = r % 2 == 0;
      if (serialNumber) {
        setCell(hasNext, fill, Alignment.center, -1, ExcelField.DEFAULT_PATTERN, r,
            defaultBorderSides);
        c++;
      }
      for (ExcelField<T, ?> excelField : excelFields) {
        setCell(hasNext, fill, excelField.align(), excelField.width(), excelField.pattern(),
            excelField.toCellValue(e), defaultBorderSides);
        c++;
      }
      c = firstColumn;
      r++;
    }
    return this;
  }

  private void setCell(boolean hasNext, boolean fill, Alignment align, double width,
      String pattern,
      Object cellValue, BorderSide... borderSide) {
    StyleSetter style = sheet.style(r, c);
    style.horizontalAlignment(align.name())
        .verticalAlignment(Alignment.center.name())
        .wrapText(wrapText)
        .format(pattern);
    for (BorderSide side : borderSide) {
      style.borderStyle(side, "thin")
          .borderColor(side, "000000");
    }
    if (fill) {
      style.fillColor("F8F8F7");
    }
    style.set();
    sheet.value(r, c, cellValue);
    if (width == -1) {
      columnWidths.put(c, cellValue.getClass().equals(Date.class) ? pattern : cellValue);
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
    int firstRow = r;
    int firstColumn = c;
    int lastColumn = serialNumber ? excelFields.length - 1 : excelFields.length - 2;
    int prevTop = r;
    int no = 1;
    ExcelField<T, ?> mergeField = excelFields[0];
    int fieldsLength = excelFields.length;
    while (iterator.hasNext()) {
      T e = converter.convert(iterator.next());

      boolean hasNext = iterator.hasNext();
      Object mergeIdValue = mergeField.toCellValue(e);
      boolean newItem = mergeId == null || !mergeId.equals(mergeIdValue);
      if (newItem) {
        fill = !fill;
        mergeId = mergeIdValue;
      }
      int prevbottom = hasNext ? r - 1 : r;
      boolean mergePrevItem = (newItem || !hasNext) && prevbottom > prevTop;
      if (serialNumber) {
        setCell(newItem, mergePrevItem, true, hasNext, fill, prevTop, prevbottom,
            firstColumn, lastColumn, Alignment.center, -1, ExcelField.DEFAULT_PATTERN, no);
        if (newItem) {
          no++;
        }
        c++;
      }

      for (int i = 1; i < fieldsLength; i++) {
        ExcelField<T, ?> excelField = excelFields[i];
        setCell(newItem, mergePrevItem, excelField.isMerge(), hasNext, fill, prevTop, prevbottom,
            firstColumn, lastColumn, excelField.align(), excelField.width(),
            excelField.pattern(), excelField.toCellValue(e));
        c++;
      }
      if (newItem) {
        prevTop = r;
      }
      c = firstColumn;
      r++;
    }
    return this;
  }

  private void setCell(boolean newItem, boolean mergePrevItem, boolean needMerge, boolean hasNext,
      boolean fill, int prevTop, int prevbottom, int firstColumn, int lastColumn,
      Alignment align,
      double width,
      String pattern,
      Object cellValue) {
    BorderSide[] borderSides;
    if (fill) {
      borderSides = defaultBorderSides;
    } else {
      List<BorderSide> borderSideList = new ArrayList<>();
      if (firstColumn == c) {
        borderSideList.add(BorderSide.LEFT);
      }
      if (lastColumn == c) {
        borderSideList.add(BorderSide.RIGHT);
      }
      if (newItem) {
        borderSideList.add(BorderSide.TOP);
      }
      if (!hasNext) {
        borderSideList.add(BorderSide.BOTTOM);
      }
      borderSides = borderSideList.toArray(new BorderSide[0]);
    }
    if (!needMerge || newItem) {
      setCell(hasNext, fill, align, width, pattern, cellValue, borderSides);
    } else {
      sheet.value(r, c);
    }

    if (mergePrevItem && needMerge) {
      sheet.range(prevTop, c, prevbottom, c).merge();
      sheet.width(c, columnWidths.width(c));
      if (!hasNext) {
        StyleSetter style = sheet.range(prevTop, c, prevbottom, c).style();
        style.horizontalAlignment(align.name())
            .verticalAlignment(Alignment.center.name())
            .wrapText(wrapText)
            .format(pattern);
        style.borderStyle(BorderSide.TOP, "thin")
            .borderColor(BorderSide.TOP, "000000")
            .borderStyle(BorderSide.BOTTOM, "thin")
            .borderColor(BorderSide.BOTTOM, "000000");
        if (fill) {
          style.borderStyle(BorderSide.LEFT, "thin")
              .borderColor(BorderSide.LEFT, "000000")
              .borderStyle(BorderSide.RIGHT, "thin")
              .borderColor(BorderSide.RIGHT, "000000");
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
