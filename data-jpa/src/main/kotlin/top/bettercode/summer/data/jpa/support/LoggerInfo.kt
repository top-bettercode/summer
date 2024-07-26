package top.bettercode.summer.data.jpa.support

import org.springframework.data.domain.Pageable

/**
 *
 * @author Peter Wu
 */
data class LoggerInfo(
    val sqlId: String,
    val annoPageInfo: PageInfo?,
    val pageableIndex: Int,
    val offsetIndex: Int,
    val isModify: Boolean,
) {
    fun pageable(args: Array<Any?>): PageInfo? {
        if (annoPageInfo != null) {
            return annoPageInfo
        }
        if (pageableIndex < 0) {
            return null
        }
        return when (val pageArg = args[pageableIndex]) {
            is Pageable -> if (pageArg.isUnpaged) null else PageInfo(
                offset = pageArg.offset,
                size = pageArg.pageSize
            )

            is Size -> PageInfo(size = pageArg.size)
            is Int -> if (offsetIndex < 0) PageInfo(size = pageArg) else PageInfo(
                offset = args[offsetIndex] as Long,
                size = pageArg
            )

            else -> null
        }
    }
}