package top.bettercode.summer.data.jpa.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import top.bettercode.summer.data.jpa.support.PageableList
import top.bettercode.summer.web.BaseController
import top.bettercode.summer.web.PagedResources
import top.bettercode.summer.web.PagedResources.PageMetadata
import top.bettercode.summer.web.RespExtra
import java.util.function.Function
import java.util.stream.Collectors

/**
 * @author Peter Wu
 */
class PageController : BaseController() {
    @Autowired
    private val properties: SpringDataWebProperties? = null
    override fun of(`object`: Any): RespExtra<*>? {
        return super.of(pagedObject(`object`)!!)
    }

    override fun ok(`object`: Any?): ResponseEntity<*> {
        return when (`object`) {
            is Page<*> -> {
                super.ok(pagedResources(`object`))
            }
            is PageableList<*> -> {
                super.ok(pagedResources(`object`.toPage()))
            }
            else -> {
                super.ok(`object`)
            }
        }
    }

    protected fun <T, R> ok(`object`: Page<T>, mapper: Function<in T, out R?>?): ResponseEntity<*> {
        val number = if (properties!!.pageable.isOneIndexedParameters) `object`.number + 1 else `object`.number
        val content = `object`.content
        return super.ok(
                PagedResources(PageMetadata(number.toLong(), `object`.size.toLong(), `object`.totalPages.toLong(),
                        `object`.totalElements),
                        content.stream().map(mapper).collect(Collectors.toList())))
    }

    protected fun page(`object`: Any?): ResponseEntity<*> {
        return super.ok(pagedObject(`object`))
    }

    private fun pagedObject(`object`: Any?): Any? {
        return if (`object` is Page<*>) {
            pagedResources(`object`)
        } else if (`object` is Collection<*>) {
            val collection = `object`
            val number = if (properties!!.pageable.isOneIndexedParameters) 1 else 0
            val size = collection.size
            PagedResources<Collection<*>>(PageMetadata(number.toLong(), size.toLong(), 1, size.toLong()), collection)
        } else if (`object` != null && `object`.javaClass.isArray) {
            val array = `object` as Array<Any>
            val number = if (properties!!.pageable.isOneIndexedParameters) 1 else 0
            val size = array.size
            PagedResources(PageMetadata(number.toLong(), size.toLong(), 1, size.toLong()), array)
        } else {
            `object`
        }
    }

    private fun pagedResources(`object`: Page<*>): PagedResources<*> {
        val number = if (properties!!.pageable.isOneIndexedParameters) `object`.number + 1 else `object`.number
        return PagedResources(PageMetadata(number.toLong(), `object`.size.toLong(), `object`.totalPages.toLong(),
                `object`.totalElements), `object`.content)
    }
}
