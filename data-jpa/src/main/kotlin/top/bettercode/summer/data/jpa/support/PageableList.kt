package top.bettercode.summer.data.jpa.support

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

/**
 * @author Peter Wu
 */
class PageableList<T>(val content: List<T>, val pageable: Pageable, val total: Long) : MutableList<T> by content.toMutableList() {
    constructor(pageable: Pageable) : this(emptyList<T>(), pageable, 0)

    //转换为org.springframework.data.domain.Page<T>
    fun toPage(): Page<T> {
        return PageImpl(this, pageable, total)
    }
}
