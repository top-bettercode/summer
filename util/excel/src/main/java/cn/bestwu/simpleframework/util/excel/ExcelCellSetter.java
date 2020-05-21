package cn.bestwu.simpleframework.util.excel;

import java.io.Serializable;

/**
 * 单元格值设置实体属性
 *
 * @param <T> 实体对象
 * @param <P> 属性
 */
@FunctionalInterface
public interface ExcelCellSetter<T, P> extends Serializable {

  void set(T entity, P property) throws NoSuchMethodException;
}