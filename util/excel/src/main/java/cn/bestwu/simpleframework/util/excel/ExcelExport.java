package cn.bestwu.simpleframework.util.excel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
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
public class ExcelExport extends AbstractExcelUtil {

  /**
   * 注解列表（ExcelFieldDescription）
   */
  private List<ExcelFieldDescription> excelFieldDescriptions;
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

  private ColumnWidths columnWidths = new ColumnWidths();

  /**
   * 构造函数
   *
   * @param os  Output stream eventually holding the serialized workbook.
   * @param cls 实体对象，通过annotation.ExportField获取标题
   */
  public ExcelExport(OutputStream os, Class<?> cls) {
    this(os, null, cls);
  }

  /**
   * 构造函数
   *
   * @param os        Output stream eventually holding the serialized workbook.
   * @param sheetname sheetname
   * @param cls       实体对象，通过annotation.ExportField获取标题
   */
  public ExcelExport(OutputStream os, String sheetname, Class<?> cls) {
    this.workbook = new Workbook(os, "", "1.0");
    initialize(cls);
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

  public void setR(int r) {
    this.r = r;
  }

  public void setC(int c) {
    this.c = c;
  }

  public void setRAndC(Integer r, Integer c) {
    this.r = r;
    this.c = c;
  }

  public void includeComment() {
    this.includeComment = true;
  }

  public void excludeComment() {
    this.includeComment = false;
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

  /**
   * @param cls 实体对象，通过annotation.ExportField获取标题
   * @return this
   */
  private ExcelExport initialize(Class<?> cls) {
    excelFieldDescriptions = getExcelFieldDescriptions(cls, ExcelFieldType.EXPORT);
    return this;
  }

  public void createHeader(Consumer<? super ExcelFieldDescription> action) {
    // Create header
    if (action != null) {
      excelFieldDescriptions.forEach(action);
    } else {
      String alignment = Alignment.CENTER.name().toLowerCase();
      for (ExcelFieldDescription excelFieldDescription : excelFieldDescriptions) {
        String t = excelFieldDescription.title();
        sheet.value(r, c, t);
        double width = excelFieldDescription.getExcelField().width();
        if (width == -1) {
          columnWidths.put(c, t);
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
          String commentStr = excelFieldDescription.comment();
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
   * 添加数据（通过annotation.ExportField添加数据）
   *
   * @param <E>  E
   * @param list list
   * @return list 数据列表
   */
  public <E> ExcelExport setDataList(Iterable<E> list) {
    Assert.notNull(sheet, "表格未初始化");
    createHeader(null);
    Iterator<E> iterator = list.iterator();
    while (iterator.hasNext()) {
      E e = iterator.next();
      boolean fill = r % 2 == 0;
      for (ExcelFieldDescription fieldDescription : excelFieldDescriptions) {
        ExcelField excelField = fieldDescription.getExcelField();
        StyleSetter style = sheet.style(r, c)
            .horizontalAlignment(excelField.align().name().toLowerCase())
            .verticalAlignment(Alignment.CENTER.name().toLowerCase())
            .wrapText(wrapText)
            .format(fieldDescription.getCellFormat())
            .borderStyle("thin").borderColor("000000");
        if (fill) {
          style.fillColor("F8F8F7");
        }
        style.set();

        Object val = fieldDescription.read(e);
        sheet.value(r, c, val);
        if (excelField.width() == -1) {
          columnWidths
              .put(c, val.getClass().equals(Date.class) ? fieldDescription.getCellFormat() : val);

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


  public ExcelExport template() throws IOException {
    includeComment = true;
    ExcelExport excelExport = setDataList(Collections.emptyList());
    excelExport.finish();
    return excelExport;
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


  public ExcelExport template(HttpServletRequest request, HttpServletResponse response,
      String fileName) throws IOException {
    write(request, response, fileName);
    return template();
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
