package top.bettercode.summer.data.jpa.support

import org.springframework.data.domain.Pageable

/**
 *
 * @author Peter Wu
 */
class PageNoCount(
    val pageable: Pageable
) : Pageable by pageable {

    fun ofCount(): Pageable {
        return pageable
    }

    companion object {

        @JvmStatic
        fun ofSize(size: Int): Pageable {
            return PageNoCount(Pageable.ofSize(size))
        }

        @JvmStatic
        fun Pageable.noCount(): PageNoCount {
            return if (this is PageNoCount) {
                this
            } else {
                PageNoCount(this)
            }
        }
    }
}