package top.bettercode.summer.data.jpa.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import top.bettercode.summer.data.jpa.support.PageableList
import top.bettercode.summer.web.BaseController
import top.bettercode.summer.web.PagedResources
import top.bettercode.summer.web.RespExtra
import java.util.function.Function
import java.util.stream.Collectors

/**
 * @author Peter Wu
 */
open class PageController : BaseController() {

    @Autowired
    private val properties: SpringDataWebProperties? = null

    override fun of(`object`: Any): RespExtra<*>? {
        return super.of(page(`object`))
    }

    fun <T> ok(`object`: Page<T?>): ResponseEntity<*> {
        return super.ok(pagedResources<T, Any>(`object`, null))
    }

    fun <T> ok(`object`: PageableList<T?>): ResponseEntity<*> {
        return super.ok(pagedResources<T, Any>(`object`.toPage(), null))
    }

    protected fun <T, R> ok(`object`: Page<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        return super.ok(pagedResources(`object`, mapper))
    }

    protected fun <T, R> ok(`object`: PageableList<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        return super.ok(pagedResources(`object`.toPage(), mapper))
    }

    protected fun <T, R> ok(`object`: Collection<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        return super.ok(`object`.stream().map(mapper).collect(Collectors.toList()))
    }

    protected fun <T, R> ok(`object`: Array<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        return super.ok(`object`.toList().stream().map(mapper).collect(Collectors.toList()))
    }

    fun <T> page(`object`: Page<T?>): ResponseEntity<*> {
        return super.ok(pagedResources<T, Any>(`object`, null))
    }

    fun <T> page(`object`: PageableList<T?>): ResponseEntity<*> {
        return super.ok(pagedResources<T, Any>(`object`.toPage(), null))
    }

    fun <T> page(`object`: Collection<T?>): ResponseEntity<*> {
        val number = if (properties!!.pageable.isOneIndexedParameters) 1 else 0
        val size = `object`.size
        val pagedResources = pagedResources(number.toLong(), size.toLong(), 1, size.toLong(), `object`)
        return super.ok(pagedResources)
    }

    fun <T> page(`object`: Array<T?>): ResponseEntity<*> {
        val number = if (properties!!.pageable.isOneIndexedParameters) 1 else 0
        val size = `object`.size
        val pagedResources = pagedResources(number.toLong(), size.toLong(), 1, size.toLong(), `object`.toList())

        return super.ok(pagedResources)
    }

    protected fun page(`object`: Any?): ResponseEntity<*> {
        return super.ok(`object`)
    }

    protected fun <T, R> page(`object`: Array<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        val number = if (properties!!.pageable.isOneIndexedParameters) 1 else 0
        val size = `object`.size
        val collect = `object`.toList().stream().map(mapper).collect(Collectors.toList())
        val pagedResources = pagedResources(number.toLong(), size.toLong(), 1, size.toLong(), collect)
        return super.ok(pagedResources)
    }

    protected fun <T, R> page(`object`: Collection<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        val number = if (properties!!.pageable.isOneIndexedParameters) 1 else 0
        val size = `object`.size
        val collect = `object`.stream().map(mapper).collect(Collectors.toList())
        val pagedResources = pagedResources(number.toLong(), size.toLong(), 1, size.toLong(), collect)
        return super.ok(pagedResources)
    }

    protected fun <T, R> page(`object`: PageableList<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        return super.ok(pagedResources(`object`.toPage(), mapper))
    }

    protected fun <T, R> page(`object`: Page<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        return super.ok(pagedResources(`object`, mapper))
    }

    @JvmOverloads
    protected fun <T, R> pagedResources(`object`: Page<T?>, mapper: Function<T?, R?>? = null): Any {
        val number = if (properties!!.pageable.isOneIndexedParameters) `object`.number + 1 else `object`.number
        val content = if (mapper == null) `object`.content else `object`.content.stream().map(mapper).collect(Collectors.toList())
        return pagedResources(number.toLong(), `object`.size.toLong(), `object`.totalPages.toLong(), `object`.totalElements, content)
    }

    protected open fun <T> pagedResources(number: Long, size: Long, totalPages: Long, totalElements: Long, content: Collection<T?>): Any {
        return PagedResources(number, size, totalPages, totalElements, content)
    }
}
