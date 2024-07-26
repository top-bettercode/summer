package top.bettercode.summer.data.jpa.support

import org.springframework.data.domain.Pageable

/**
 *
 * @author Peter Wu
 */
data class LoggerInfo(
    val sqlId: String,
    val annoPageable: Pageable?,
    val pageableIndex: Int,
    val isModify: Boolean,
) {
    fun pageable(args: Array<Any?>): Pageable? {
        if (annoPageable != null) {
            return annoPageable
        }
        if (pageableIndex < 0) {
            return null
        }
        return when (val pageArg = args[pageableIndex]) {
            is Pageable -> pageArg
            is Size -> Pageable.ofSize(pageArg.size)
            is Int -> Pageable.ofSize(pageArg)
            else -> null
        }
    }
}