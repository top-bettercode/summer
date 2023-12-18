package top.bettercode.summer.data.jpa.support

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

/**
 *
 * @author Peter Wu
 */
class PageSize(
    val pageable: Pageable
) : Pageable by pageable {

    companion object {

        @JvmStatic
        fun of(size: Int): Pageable {
            return PageSize(PageRequest.of(0, size))
        }

        @JvmStatic
        fun Pageable.size(): Pageable {
            return if (this is PageSize) {
                this
            } else {
                PageSize(this)
            }
        }
    }
}