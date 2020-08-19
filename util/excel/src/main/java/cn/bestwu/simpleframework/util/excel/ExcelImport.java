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
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

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
  private int rowNum;

  /**
   * 构造函数
   *
   * @param fileName 导入文件
   * @throws IOException IOException
   */
  public ExcelImport(String fileName)
      throws IOException {
    this(new File(fileName));
  }

  /**
   * 构造函数
   *
   * @param file 导入文件对象
   * @throws IOException IOException
   */
  public ExcelImport(File file)
      throws IOException {
    this(new FileInputStream(file));
  }

  /**
   * 构造函数
   *
   * @param multipartFile 导入文件对象
   * @throws IOException IOException
   */
  public ExcelImport(MultipartFile multipartFile)
      throws IOException {
    this(multipartFile.getInputStream());
  }

  /**
   * 构造函数
   *
   * @param is is
   * @throws IOException IOException
   */
  public ExcelImport(InputStream is)
      throws IOException {
    workbook = new ReadableWorkbook(is);
    log.debug("Initialize success.");
  }

  public ReadableWorkbook getWorkbook() {
    return workbook;
  }

  public int getRowNum() {
    return rowNum;
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
      throws IOException, IllegalAccessException, InstantiationException, ExcelImportException {
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
      throws IOException, IllegalAccessException, InstantiationException, ExcelImportException {
    return getData(0, 0, excelFields[0].entityType, excelFields, converter);
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
      throws IOException, IllegalAccessException, InstantiationException, ExcelImportException {
    return getData(cls, excelFields, (o) -> (E) o);
  }


  /**
   * 获取导入数据列表
   *
   * @param converter   F 转换为E
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
  public <F, E> List<E> getData(Class<F> cls, ExcelField<F, ?>[] excelFields,
      ExcelConverter<F, E> converter)
      throws IOException, IllegalAccessException, InstantiationException, ExcelImportException {
    return getData(0, 0, cls, excelFields, converter);
  }


  /**
   * 获取导入数据列表
   *
   * @param headerNum   标题行号，数据行号=标题行号+1
   * @param sheetIndex  工作表编号
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
  public <F, E> List<E> getData(int sheetIndex, int headerNum, Class<F> cls,
      ExcelField<F, ?>[] excelFields,
      ExcelConverter<F, E> converter)
      throws IOException, IllegalAccessException, ExcelImportException, InstantiationException {
    Sheet sheet = workbook.getSheet(sheetIndex).orElse(null);
    if (sheet == null) {
      throw new RuntimeException("文档中未找到相应工作表!");
    }
    List<E> dataList = new ArrayList<>();
    for (Row row : sheet.openStream().filter(r -> r.getRowNum() - 1 > headerNum)
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
      throws InstantiationException, IllegalAccessException, ExcelImportException {
    boolean notAllBlank = false;
    int column = 0;
    F o = cls.newInstance();
    List<CellError> rowErrors = new ArrayList<>();
    rowNum = row.getRowNum();

    for (ExcelField<F, ?> excelField : excelFields) {
      Object cellValue = excelField.getCellValue(row, column++);
      notAllBlank = notAllBlank || !excelField.isEmptyCell(cellValue);
      try {
        excelField.setProperty(o, cellValue, validator, validateGroups);
      } catch (Exception e) {
        rowErrors.add(new CellError(rowNum, column - 1, excelField.title(),
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
   * @param validateGroups 验证 groups
   * @return ExcelImport this
   */
  public ExcelImport validateGroups(Class<?>... validateGroups) {
    this.validateGroups = validateGroups;
    return this;
  }


}
