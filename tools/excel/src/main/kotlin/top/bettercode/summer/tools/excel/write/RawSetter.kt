package top.bettercode.summer.tools.excel.write


/**
 * 原值设置到cell
 *
 * @author Peter Wu
 */
open class RawSetter<E, P>(title: String, val value: Any?) : CellSetter<E, P>(title) {

    override fun toCell(obj: E): Any? {
        return value
    }
}