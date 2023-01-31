package top.bettercode.summer.tools.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dhatim.fastexcel.AbsoluteListDataValidation;
import org.dhatim.fastexcel.StyleSetter;
import org.dhatim.fastexcel.TimestampUtil;
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


  private final ByteArrayOutputStream imageByteArrayOutputStream;

  private final OutputStream outputStream;

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
  private boolean finish = false;
  private boolean includeDataValidation = false;

  private final ColumnWidths columnWidths = new ColumnWidths();

  private final List<ExcelCell<?>> imageCells = new ArrayList<>();


  /**
   * @param filename filename eventually holding the serialized workbook .
   * @return ExcelExport
   * @throws FileNotFoundException FileNotFoundException
   */
  public static ExcelExport of(String filename) throws IOException {
    return new ExcelExport(Files.newOutputStream(Paths.get(filename)));
  }

  /**
   * @param file filename eventually holding the serialized workbook .
   * @return ExcelExport
   * @throws FileNotFoundException FileNotFoundException
   */
  public static ExcelExport of(File file) throws IOException {
    File parentFile = file.getParentFile();
    if (!parentFile.exists()) {
      parentFile.mkdirs();
    }
    return new ExcelExport(Files.newOutputStream(file.toPath()));
  }


  /**
   * @param outputStream Output stream eventually holding the serialized workbook.
   * @return ExcelExport
   */
  public static ExcelExport of(OutputStream outputStream) {
    return new ExcelExport(outputStream);
  }

  public static ExcelExport withImage(OutputStream outputStream) {
    return new ExcelExport(outputStream, new ByteArrayOutputStream());
  }


  /**
   * 构造函数
   *
   * @param outputStream          Output stream eventually holding the serialized workbook.
   * @param byteArrayOutputStream byteArrayOutputStream
   */
  private ExcelExport(OutputStream outputStream, ByteArrayOutputStream byteArrayOutputStream) {
    this.imageByteArrayOutputStream = byteArrayOutputStream;
    this.outputStream = outputStream;
    this.workbook = new Workbook(byteArrayOutputStream, "", "1.0");
  }

  /**
   * 构造函数
   *
   * @param outputStream Output stream eventually holding the serialized workbook.
   */
  private ExcelExport(OutputStream outputStream) {
    this.imageByteArrayOutputStream = null;
    this.outputStream = null;
    this.workbook = new Workbook(outputStream, "", "1.0");
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
  public ExcelExport setRowAndColumn(int row, int column) {
    this.r = row;
    this.c = column;
    return this;
  }

  public ExcelExport includeDataValidation() {
    this.includeDataValidation = true;
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
        if (imageByteArrayOutputStream == null && excelField.isImageColumn()) {
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
        sheet.range(r + 1, c, r + 1000, c).style().format(excelField.format());
        if (includeComment) {
          String commentStr = excelField.comment();
          if (StringUtils.hasText(commentStr)) {
            sheet.comment(r, c, commentStr);
          }
        }
        if (includeDataValidation && StringUtils.hasText(excelField.dataValidation())) {
          AbsoluteListDataValidation listDataValidation = new AbsoluteListDataValidation(
              sheet.range(r + 1, c, Worksheet.MAX_ROWS - 1, c), excelField.dataValidation());
          listDataValidation.add(sheet);
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

  public ExcelExport dataValidation(int column, Collection<String> dataValidation) {
    return dataValidation(column, StringUtils.collectionToCommaDelimitedString(dataValidation));
  }

  public ExcelExport dataValidation(int column, String dataValidation) {
    Assert.notNull(sheet, "请先初始化sheet");
    AbsoluteListDataValidation listDataValidation = new AbsoluteListDataValidation(
        sheet.range(r + 1, column, Worksheet.MAX_ROWS - 1, column), dataValidation);
    listDataValidation.add(sheet);
    return this;
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
        if (imageByteArrayOutputStream == null && excelField.isImageColumn()) {
          continue;
        }
        setCell(new ExcelCell<>(r, c, firstRow, lastRow, excelField, e));
        c++;
      }
      c = firstColumn;
      r++;
    }
    return this;
  }

  private <T> void setCell(ExcelCell<T> excelCell) {
    int column = excelCell.getColumn();
    int row = excelCell.getRow();
    StyleSetter style = sheet.style(row, column);
    ExcelField<T, ?> excelField = excelCell.getExcelField();
    String format = excelField.format();
    style.horizontalAlignment(excelField.align().name())
        .verticalAlignment(Alignment.center.name())
        .wrapText(wrapText)
        .format(format)
        .borderStyle("thin")
        .borderColor("000000");

    if (excelCell.isFillColor()) {
      style.fillColor("F8F8F7");
    }
    if (excelField.height() != -1) {
      sheet.rowHeight(row, excelField.height());
    }
    style.set();

    if (excelField.isImageColumn()) {
      imageCells.add(excelCell);
    }
    if (excelCell.needSetValue()) {
      Object cellValue = excelCell.getCellValue();
      if (cellValue == null) {
        sheet.value(row, column);
      } else if (excelField.isImageColumn()) {
        sheet.value(excelCell.getRow(), column);
      } else if (cellValue instanceof String) {
        sheet.value(row, column, (String) cellValue);
      } else if (cellValue instanceof Number) {
        sheet.value(row, column, (Number) cellValue);
      } else if (cellValue instanceof Boolean) {
        sheet.value(row, column, (Boolean) cellValue);
      } else if (cellValue instanceof Date) {
        sheet.value(row, column, TimestampUtil.convertDate((Date) cellValue));
      } else if (cellValue instanceof LocalDateTime) {
        sheet.value(row, column, TimestampUtil.convertDate(
            Date.from(((LocalDateTime) cellValue).atZone(ZoneId.systemDefault()).toInstant())));
      } else if (cellValue instanceof LocalDate) {
        sheet.value(row, column, TimestampUtil.convertDate((LocalDate) cellValue));
      } else if (cellValue instanceof ZonedDateTime) {
        sheet.value(row, column, TimestampUtil.convertZonedDateTime((ZonedDateTime) cellValue));
      } else {
        throw new IllegalArgumentException("No supported cell type for " + cellValue.getClass());
      }
    } else {
      sheet.value(excelCell.getRow(), column);
    }

    double width = excelField.width();
    if (width == -1) {
      Object cellValue = excelCell.getCellValue();
      columnWidths.put(column, excelField.isDateField() ? format : cellValue);
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
    ExcelField<T, ?> firstField = excelFields[0];
    if (imageByteArrayOutputStream == null) {
      firstField = Arrays.stream(excelFields).filter(o -> !o.isImageColumn()).findFirst()
          .orElseThrow(() -> new ExcelException("无可导出项目"));
    } else {
      firstField = excelFields[0];
    }
    boolean mergeFirstColumn = firstField.isMerge();
    Map<Integer, Object> lastMergeIds = new HashMap<>();
    Map<Integer, Integer> lastRangeTops = new HashMap<>();
    T preEntity = null;
    while (iterator.hasNext()) {
      T e = converter.convert(iterator.next());
      boolean lastRow = !iterator.hasNext();

      int mergeIndex = 0;
      List<ExcelRangeCell<T>> indexCells = mergeFirstColumn ? null : new ArrayList<>();
      boolean merge;
      for (ExcelField<T, ?> excelField : excelFields) {
        if (imageByteArrayOutputStream == null && excelField.isImageColumn()) {
          continue;
        }
        merge = excelField.isMerge();
        if (merge) {
          Object mergeIdValue = excelField.mergeId(e);
          Object lastMergeId = lastMergeIds.get(mergeIndex);
          boolean newRange = lastMergeId == null || !lastMergeId.equals(mergeIdValue);
          if (newRange) {
            lastMergeIds.put(mergeIndex, mergeIdValue);
          }

          if (mergeIndex == 0 && newRange) {
            index++;
          }
          if (lastRangeTops.getOrDefault(0, firstRow) == r) {//以第一个合并列为大分隔
            newRange = true;
          }

          int lastRangeTop = lastRangeTops.getOrDefault(mergeIndex, firstRow);

          if (newRange) {
            lastRangeTops.put(mergeIndex, r);
          }

          ExcelRangeCell<T> rangeCell = new ExcelRangeCell<>(r, c, index, firstRow, lastRow,
              newRange, lastRangeTop, excelField, preEntity, e);
          setRangeCell(rangeCell);

          mergeIndex++;
        } else {
          ExcelRangeCell<T> rangeCell = new ExcelRangeCell<>(r, c, index, firstRow, lastRow,
              false, r, excelField, preEntity, e);
          if (!mergeFirstColumn && !merge && excelField.isIndexColumn()) {
            indexCells.add(rangeCell);
          } else {
            setRangeCell(rangeCell);
          }
        }
        c++;
      }
      if (!mergeFirstColumn) {
        for (ExcelRangeCell<T> indexCell : indexCells) {
          indexCell.setFillColor(index);
          setRangeCell(indexCell);
        }
      }
      c = firstColumn;
      preEntity = e;
      r++;
    }
    return this;
  }

  private <T> void setRangeCell(ExcelRangeCell<T> excelCell) {
    int column = excelCell.getColumn();
    setCell(excelCell);

    if (excelCell.needRange()) {
      sheet.range(excelCell.getLastRangeTop(), column, excelCell.getLastRangeBottom(), column)
          .merge();
      ExcelField<T, ?> excelField = excelCell.getExcelField();
      double width = excelField.width();
      String format = excelField.format();
      if (width == -1) {
        sheet.width(column, columnWidths.width(column));
      } else {
        sheet.width(column, width);
      }
      StyleSetter style = sheet
          .range(excelCell.getLastRangeTop(), column, excelCell.getLastRangeBottom(), column)
          .style();
      style.horizontalAlignment(excelField.align().name())
          .verticalAlignment(Alignment.center.name())
          .wrapText(wrapText)
          .format(format)
          .borderStyle("thin")
          .borderColor("000000");

      style.set();
    }
  }


  public <T> ExcelExport template(ExcelField<T, ?>[] excelFields) {
    includeComment = true;
    setData(Collections.emptyList(), excelFields);
    return this;
  }

  /**
   * 输出数据流
   */
  public ExcelExport finish() {
    if (!finish) {
      try {
        workbook.finish();
        finish = true;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }


  public void setImage() {
    setImage(sheet.getName());
  }

  /**
   * @param sheetName sheetName
   */
  public void setImage(String sheetName) {
    Assert.notNull(imageByteArrayOutputStream, "不是支持图片插入的导出");
    ExcelImageCellWriterUtil.setImage(sheetName, imageCells,
        new ByteArrayInputStream(imageByteArrayOutputStream.toByteArray()), outputStream);
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
    setResponseHeader(request, response, fileName);
    ExcelExport excelExport = ExcelExport.of(response.getOutputStream());
    consumer.accept(excelExport);
    excelExport.finish();
  }

  /**
   * 输出数据流
   *
   * @param fileName 输出文件名
   * @param consumer 处理生成excel
   * @throws IOException IOException
   */
  public static void exportWithImage(String fileName, Consumer<ExcelExport> consumer)
      throws IOException {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    Assert.notNull(requestAttributes, "requestAttributes获取失败");
    HttpServletRequest request = requestAttributes.getRequest();
    HttpServletResponse response = requestAttributes.getResponse();
    setResponseHeader(request, response, fileName);
    ExcelExport excelExport = ExcelExport.withImage(response.getOutputStream());
    consumer.accept(excelExport);
    excelExport.finish();
  }

  /**
   * 输出数据流
   *
   * @param fileName 输出文件名
   * @param consumer 处理生成excel
   * @throws IOException IOException
   */
  public static void sheet(String fileName, Consumer<ExcelExport> consumer) throws IOException {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    Assert.notNull(requestAttributes, "requestAttributes获取失败");
    HttpServletRequest request = requestAttributes.getRequest();
    HttpServletResponse response = requestAttributes.getResponse();
    setResponseHeader(request, response, fileName);
    ExcelExport excelExport = ExcelExport.of(response.getOutputStream());
    excelExport.sheet("sheet1");
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
    cacheOutput(fileName, fileKey, outputStream -> {
      ExcelExport excelExport = ExcelExport.of(outputStream);
      consumer.accept(excelExport);
      excelExport.finish();
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
    setResponseHeader(request, response, fileName);
    String tmpPath = System.getProperty("java.io.tmpdir");

    File file = new File(tmpPath,
        "excel-export" + File.separator + fileName + File.separator + fileKey + ".xlsx");
    if (!file.exists()) {
      File dir = file.getParentFile();
      if (!dir.exists()) {
        dir.mkdirs();
      }
      File tmpFile = new File(file + "-" + UUID.randomUUID());
      try (OutputStream outputStream = Files.newOutputStream(tmpFile.toPath())) {
        consumer.accept(outputStream);
      }
      tmpFile.renameTo(file);
    }
    StreamUtils.copy(Files.newInputStream(file.toPath()), response.getOutputStream());
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
      newFileName = fileName;
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
