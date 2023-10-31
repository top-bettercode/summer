package top.bettercode.summer.data.jpa.web

/**
 * @author Peter Wu
 */
open class CompatiblePageController : PageController() {

    override fun <T> pagedResources(number: Long, size: Long, totalPages: Long, totalElements: Long, content: T?): Any {
        return CompatiblePagedResources(number, size, totalPages, totalElements, content)
    }

}
