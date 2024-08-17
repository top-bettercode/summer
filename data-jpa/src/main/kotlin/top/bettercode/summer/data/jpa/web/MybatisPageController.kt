package top.bettercode.summer.data.jpa.web

/**
 * @author Peter Wu
 */
open class MybatisPageController : PageController() {

    override fun <T> pagedResources(number: Int, size: Int, totalPages: Int, totalElements: Long, content: Collection<T?>): Any {
        return MybatisPagedResources(number, size, totalPages, totalElements, content)
    }

}
