package top.bettercode.summer.tools.excel

import java.io.Serializable

/**
 * 转换器
 *
 * @param <F> 源
 * @param <T> 目标
</T></F> */
@FunctionalInterface
fun interface Converter<F, T> : Serializable {

    fun convert(from: F): T

}