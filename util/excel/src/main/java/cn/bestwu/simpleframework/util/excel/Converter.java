package cn.bestwu.simpleframework.util.excel;

/**
 * 转换器
 *
 * @param <F> 源
 * @param <T> 目标
 */
@FunctionalInterface
public interface Converter<F, T> {

  T convert(F from);
}