package cn.bestwu.simpleframework.util.excel;

import cn.bestwu.simpleframework.util.excel.ExcelImportException.CellError;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 导入Excel文件
 */
public class ExcelImport extends AbstractExcelUtil {

  private static final Logger log = LoggerFactory.getLogger(ExcelImport.class);
  private static final Validator validator = Validation.buildDefaultValidatorFactory()
      .getValidator();
  /**
   * 工作表对象
   */
  private final ReadableWorkbook workbook;
  private final List<ExcelFieldDescription> fieldDescriptions;
  /**
   * 验证 groups
   */
  private Class<?>[] validateGroups = new Class[]{Default.class};

  /**
   * 构造函数
   *
   * @param fileName 导入文件
   * @param cls      execl字段映射类
   * @throws IOException IOException
   */
  public ExcelImport(String fileName, Class<?> cls)
      throws IOException {
    this(new File(fileName), cls);
  }

  /**
   * 构造函数
   *
   * @param file 导入文件对象
   * @param cls  execl字段映射类
   * @throws IOException IOException
   */
  public ExcelImport(File file, Class<?> cls)
      throws IOException {
    this(new FileInputStream(file), cls);
  }

  /**
   * 构造函数
   *
   * @param multipartFile 导入文件对象
   * @param cls           execl字段映射类
   * @throws IOException IOException
   */
  public ExcelImport(MultipartFile multipartFile, Class<?> cls)
      throws IOException {
    this(multipartFile.getInputStream(), cls);
  }

  /**
   * 构造函数
   *
   * @param is  is
   * @param cls execl字段映射类
   * @throws IOException IOException
   */
  public ExcelImport(InputStream is, Class<?> cls)
      throws IOException {
    workbook = new ReadableWorkbook(is);
    fieldDescriptions = getExcelFieldDescriptions(cls, ExcelFieldType.IMPORT);
    log.debug("Initialize success.");
  }

  public ReadableWorkbook getWorkbook() {
    return workbook;
  }

  /**
   * 获取导入数据列表
   *
   * @param cls 导入对象类型
   * @param <F> F
   * @param <E> E
   * @return List
   * @throws IOException            IOException
   * @throws IllegalAccessException IllegalAccessException
   * @throws ExcelImportException   ExcelImportException
   * @throws InstantiationException InstantiationException
   */
  @SuppressWarnings("unchecked")
  public <F, E> List<E> getDataList(Class<F> cls)
      throws IOException, IllegalAccessException, InstantiationException, ExcelImportException {
    return getDataList(cls, (o) -> (E) o);
  }


  /**
   * 获取导入数据列表
   *
   * @param cls       导入对象类型
   * @param converter F 转换为E
   * @param <F>       F
   * @param <E>       E
   * @return List
   * @throws IOException            IOException
   * @throws IllegalAccessException IllegalAccessException
   * @throws ExcelImportException   ExcelImportException
   * @throws InstantiationException InstantiationException
   */
  public <F, E> List<E> getDataList(Class<F> cls, Converter<F, E> converter)
      throws IOException, IllegalAccessException, InstantiationException, ExcelImportException {
    return getDataList(0, 0, cls, converter);
  }


  /**
   * 获取导入数据列表
   *
   * @param cls        导入对象类型
   * @param headerNum  标题行号，数据行号=标题行号+1
   * @param sheetIndex 工作表编号
   * @param converter  F 转换为E
   * @param <F>        F
   * @param <E>        E
   * @return List
   * @throws IOException            IOException
   * @throws IllegalAccessException IllegalAccessException
   * @throws ExcelImportException   ExcelImportException
   * @throws InstantiationException InstantiationException
   */
  public <F, E> List<E> getDataList(int sheetIndex, int headerNum, Class<F> cls,
      Converter<F, E> converter)
      throws IOException, IllegalAccessException, ExcelImportException, InstantiationException {
    Sheet sheet = workbook.getSheet(sheetIndex).orElse(null);
    if (sheet == null) {
      throw new RuntimeException("文档中未找到相应工作表!");
    }
    List<E> dataList = new ArrayList<>();
    for (Row row : sheet.openStream().filter(r -> r.getRowNum() - 1 > headerNum)
        .collect(Collectors.toList())) {
      if (row != null) {
        E e = readRow(fieldDescriptions, cls, row, converter);
        if (e != null) {
          dataList.add(e);
        }
      }
    }
    return dataList;
  }

  public <F, E> E readRow(List<ExcelFieldDescription> fieldDescriptions, Class<F> cls, Row row,
      Converter<F, E> converter)
      throws InstantiationException, IllegalAccessException, ExcelImportException {
    boolean notAllBlank = false;
    int column = 0;
    F o = cls.newInstance();
    List<CellError> rowErrors = new ArrayList<>();
    int rowNum = row.getRowNum() + 1;
    for (ExcelFieldDescription fieldDescription : fieldDescriptions) {
      Object val = getCellValue(row, column++);
      if (val != null) {
        String valStr = String.valueOf(val).trim();
        notAllBlank = notAllBlank || StringUtils.hasText(valStr);
        try {
          fieldDescription.write(o, valStr, validator, validateGroups);
        } catch (Exception ex) {
          rowErrors.add(new CellError(rowNum, column - 1, fieldDescription.title(), valStr, ex));
        }
      }
    }
    if (notAllBlank) {
      if (!rowErrors.isEmpty()) {
        throw new ExcelImportException(rowErrors.get(0).getException().getMessage(), rowErrors);
      }
      return converter.convert(o);
    } else {
      return null;
    }
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
   * 获取单元格值
   *
   * @param row    获取的行
   * @param column 获取单元格列号
   * @return 单元格值
   */
  public static Object getCellValue(Row row, int column) {
    Cell cell = row.getCell(column);
    if (cell != null) {
      switch (cell.getType()) {
        case STRING:
          return row.getCellAsString(column).orElse(null);
        case NUMBER:
          return row.getCellAsNumber(column).orElse(null);
        case BOOLEAN:
          return row.getCellAsBoolean(column).orElse(null);
        case EMPTY:
          return null;
        case FORMULA:
        case ERROR:
          return row.getCellText(column);
      }
    }
    return null;
  }
}
