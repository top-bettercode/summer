package top.bettercode.summer.tools.excel

import java.io.Serializable

/**
 * 单元格值设置实体属性
 *
 * @param <T> 实体对象
 * @param <P> 属性
</P></T> */
@FunctionalInterface
fun interface ExcelCellSetter<T, P> : Serializable {

    operator fun set(entity: T, property: P?)

}