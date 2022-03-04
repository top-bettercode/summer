package top.bettercode.util.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import top.bettercode.util.excel.ExcelImportException.CellError;

/**
 * 导入Excel文件
 */
public class ExcelImport {

  private static final Logger log = LoggerFactory.getLogger(ExcelImport.class);
  private static final Validator validator = Validation.buildDefaultValidatorFactory()
      .getValidator();
  /**
   * 工作表对象
   */
  private final ReadableWorkbook workbook;
  /**
   * 验证 groups
   */
  private Class<?>[] validateGroups = new Class[]{Default.class};
  /**
   * 当前行号
   */
  private int r = 1;
  /**
   * 当前单元格号
   */
  private int c = 0;
  /**
   * 工作表对象
   */
  private Sheet sheet;


  /**
   * @param fileName 导入文件
   * @return ExcelImport
   * @throws IOException IOException
   */
  public static ExcelImport of(String fileName)
      throws IOException {
    return new ExcelImport(new FileInputStream(fileName));
  }

  /**
   * @param file 导入文件对象
   * @return ExcelImport
   * @throws IOException IOException
   */
  public static ExcelImport of(File file)
      throws IOException {
    return new ExcelImport(new FileInputStream(file));
  }

  /**
   * @param multipartFile 导入文件对象
   * @return ExcelImport
   * @throws IOException IOException
   */
  public static ExcelImport of(MultipartFile multipartFile)
      throws IOException {
    return new ExcelImport(multipartFile.getInputStream());
  }

  /**
   * 构造函数
   *
   * @param is is
   * @throws IOException IOException
   */
  private ExcelImport(InputStream is)
      throws IOException {
    workbook = new ReadableWorkbook(is);
    sheet = workbook.getFirstSheet();
    setRowAndColumn(1, 0);
    log.debug("Initialize success.");
  }

  public ReadableWorkbook getWorkbook() {
    return workbook;
  }


  /**
   * @param row 行号，从0开始
   * @return this ExcelExport
   */
  public ExcelImport setRow(int row) {
    this.r = row;
    return this;
  }

  /**
   * @param column 列号，从0开始
   * @return this ExcelExport
   */
  public ExcelImport setColumn(int column) {
    this.c = column;
    return this;
  }

  /**
   * @param row    行号，从0开始
   * @param column 列号，从0开始
   * @return this ExcelExport
   */
  public ExcelImport setRowAndColumn(Integer row, Integer column) {
    this.r = row;
    this.c = column;
    return this;
  }

  public int getRow() {
    return r;
  }

  public int getColumn() {
    return c;
  }

  public ExcelImport sheet(int sheetIndex) {
    this.sheet = workbook.getSheet(sheetIndex)
        .orElseThrow(() -> new ExcelException("未找到第" + (sheetIndex + 1) + "张表"));
    setRowAndColumn(1, 0);
    return this;
  }

  public ExcelImport sheet(String sheetName) {
    this.sheet = workbook.findSheet(sheetName)
        .orElseThrow(() -> new ExcelException("未找到表：" + sheetName));
    setRowAndColumn(1, 0);
    return this;
  }


  /**
   * @param validateGroups 验证 groups
   * @return ExcelImport this
   */
  public ExcelImport validateGroups(Class<?>... validateGroups) {
    this.validateGroups = validateGroups;
    return this;
  }

  /**
   * 获取导入数据列表
   *
   * @param <F>         F
   * @param <E>         E
   * @param excelFields excelFields
   * @return List
   * @throws IOException            IOException
   * @throws IllegalAccessException IllegalAccessException
   * @throws ExcelImportException   ExcelImportException
   * @throws InstantiationException InstantiationException
   */
  public <F, E> List<E> getData(ExcelField<F, ?>[] excelFields)
      throws Exception {
    return getData(excelFields[0].entityType, excelFields);
  }


  /**
   * 获取导入数据列表
   *
   * @param converter   F 转换为E
   * @param <F>         F
   * @param <E>         E
   * @param excelFields excelFields
   * @return List
   * @throws IOException            IOException
   * @throws IllegalAccessException IllegalAccessException
   * @throws ExcelImportException   ExcelImportException
   * @throws InstantiationException InstantiationException
   */
  public <F, E> List<E> getData(ExcelField<F, ?>[] excelFields, ExcelConverter<F, E> converter)
      throws Exception {
    return getData(excelFields[0].entityType, excelFields, converter);
  }

  /**
   * 获取导入数据列表
   *
   * @param <F>         F
   * @param <E>         E
   * @param excelFields excelFields
   * @param cls         实体类型
   * @return List
   * @throws IOException            IOException
   * @throws IllegalAccessException IllegalAccessException
   * @throws ExcelImportException   ExcelImportException
   * @throws InstantiationException InstantiationException
   */
  @SuppressWarnings("unchecked")
  public <F, E> List<E> getData(Class<F> cls, ExcelField<F, ?>[] excelFields)
      throws Exception {
    return getData(cls, excelFields, (o) -> (E) o);
  }

  /**
   * 获取导入数据列表
   *
   * @param cls         实体类型
   * @param excelFields excelFields
   * @param converter   F 转换为E
   * @param <F>         F
   * @param <E>         E
   * @return List
   * @throws IOException            IOException
   * @throws IllegalAccessException IllegalAccessException
   * @throws ExcelImportException   ExcelImportException
   * @throws InstantiationException InstantiationException
   */
  public <F, E> List<E> getData(Class<F> cls, ExcelField<F, ?>[] excelFields,
      ExcelConverter<F, E> converter)
      throws Exception {
    if (sheet == null) {
      throw new RuntimeException("文档中未找到相应工作表!");
    }
    List<E> dataList = new ArrayList<>();
    for (Row row : sheet.openStream().filter(row -> row.getRowNum() > r)
        .collect(Collectors.toList())) {
      if (row != null) {
        E e = readRow(cls, excelFields, row, converter);
        if (e != null) {
          dataList.add(e);
        }
      }
    }
    return dataList;
  }

  public <F, E> E readRow(Class<F> cls, ExcelField<F, ?>[] excelFields, Row row,
      ExcelConverter<F, E> converter)
      throws Exception {
    boolean notAllBlank = false;
    int column = c;
    F o = cls.getDeclaredConstructor().newInstance();
    List<CellError> rowErrors = new ArrayList<>();
    r = row.getRowNum();

    for (ExcelField<F, ?> excelField : excelFields) {
      Object cellValue = getCellValue(excelField, row, column++);
      notAllBlank = notAllBlank || !excelField.isEmptyCell(cellValue);
      try {
        excelField.setProperty(o, cellValue, validator, validateGroups);
      } catch (Exception e) {
        rowErrors.add(new CellError(r, column - 1, excelField.title(),
            (cellValue == null ? null : String.valueOf(cellValue)), e));
      }
    }
    if (notAllBlank) {
      if (!rowErrors.isEmpty()) {
        Exception exception = rowErrors.get(0).getException();
        throw new ExcelImportException(exception.getMessage(), rowErrors, exception);
      }
      return converter.convert(o);
    } else {
      return null;
    }
  }


  /**
   * 获取单元格值
   *
   * @param row    获取的行
   * @param column 获取单元格列号
   * @return 单元格值
   */
  private Object getCellValue(ExcelField<?, ?> excelField, Row row, int column) {
    try {
      Cell cell = row.getCell(column);
      if (cell != null) {
        switch (cell.getType()) {
          case STRING:
            return row.getCellAsString(column).orElse(null);
          case NUMBER:
            if (excelField.isDateField()) {
              return row.getCellAsDate(column).orElse(null);
            } else {
              return row.getCellAsNumber(column).orElse(null);
            }
          case BOOLEAN:
            return row.getCellAsBoolean(column).orElse(null);
          case FORMULA:
          case EMPTY:
          case ERROR:
            return row.getCell(column).getValue();
        }
      }
    } catch (IndexOutOfBoundsException ignored) {
    }
    return null;
  }

}
