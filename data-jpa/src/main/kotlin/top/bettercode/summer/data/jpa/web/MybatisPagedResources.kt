package top.bettercode.summer.data.jpa.web

import com.fasterxml.jackson.annotation.JsonView
import org.springframework.data.domain.Page

/**
 * @author Peter Wu
 */
class MybatisPagedResources<T>(
        /**
         * 当前页
         */
        @field:JsonView(Any::class) var page: Int,
        /**
         * 每页显示条数，默认 10
         */
        @field:JsonView(Any::class) var size: Int,
        @field:JsonView(Any::class) var pages: Int,
        /**
         * 总数
         */
        @field:JsonView(Any::class) var total: Long,
        @field:JsonView(Any::class) var list: Collection<T>) {
    constructor(page: Page<T>) : this(page.number, page.size, page.totalPages, page.totalElements,
            page.content)

}
