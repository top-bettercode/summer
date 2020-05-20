package cn.bestwu.simpleframework.util.excel;

import java.io.Serializable;

/**
 * 转换器
 *
 * @param <F> 源
 * @param <T> 目标
 */
@FunctionalInterface
public interface ExcelConverter<F, T> extends Serializable {

  T convert(F from);
}