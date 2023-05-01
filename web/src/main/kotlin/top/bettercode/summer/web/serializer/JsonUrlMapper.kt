package top.bettercode.summer.web.serializer

/**
 * @author Peter Wu
 */
interface JsonUrlMapper {
    /**
     * @param obj 对象
     * @return 字符串
     */
    fun mapper(obj: Any?): String? {
        return obj?.toString()?.trim { it <= ' ' }
    }
}
