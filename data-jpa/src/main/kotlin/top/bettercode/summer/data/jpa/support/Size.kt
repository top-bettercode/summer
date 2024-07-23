package top.bettercode.summer.data.jpa.support

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * @author Peter Wu
 */
open class Size private constructor(val size: Int, val sort: Sort) {

    fun isUnlimited(): Boolean {
        return size == -1
    }

    companion object {

        private val UNLIMITED: Size = Size(-1, sort = Sort.unsorted())

        @JvmStatic
        fun unlimited(): Size {
            return UNLIMITED
        }

        @JvmStatic
        fun of(pageable: Pageable): Size {
            return of(pageable.pageSize, pageable.sort)
        }

        @JvmStatic
        @JvmOverloads
        fun of(size: Int, sort: Sort = Sort.unsorted()): Size {
            return Size(size, sort)
        }
    }
}
