package top.bettercode.summer.data.jpa.web

/**
 * @author Peter Wu
 */
open class MybatisPageController : PageController() {

    override fun <T> pagedResources(number: Long, size: Long, totalPages: Long, totalElements: Long, content: Collection<T?>): Any {
        return MybatisPagedResources(number, size, totalPages, totalElements, content)
    }

}
