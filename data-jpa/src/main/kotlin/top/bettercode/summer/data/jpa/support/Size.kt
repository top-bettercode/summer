package top.bettercode.summer.data.jpa.support

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * @author Peter Wu
 */
class Size private constructor(val size: Int, val sort: Sort) {

    companion object {
        fun of(pageable: Pageable): Size {
            return of(pageable.pageSize, pageable.sort)
        }

        @JvmOverloads
        fun of(size: Int, sort: Sort = Sort.unsorted()): Size {
            return Size(size, sort)
        }
    }
}
