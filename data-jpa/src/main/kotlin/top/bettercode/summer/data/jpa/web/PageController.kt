package top.bettercode.summer.data.jpa.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
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
    private lateinit var properties: SpringDataWebProperties

    @JvmOverloads
    protected fun <T, R> ok(
        `object`: Page<T?>,
        mapper: Function<T?, R?>? = null
    ): ResponseEntity<*> {
        return super.ok(pagedResources(`object`, mapper))
    }

    protected fun <T, R> ok(`object`: Collection<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        return super.ok(`object`.stream().map(mapper).collect(Collectors.toList()))
    }

    protected fun <T, R> ok(`object`: Array<T?>, mapper: Function<T?, R?>): ResponseEntity<*> {
        return super.ok(`object`.toList().stream().map(mapper).collect(Collectors.toList()))
    }

    protected fun page(`object`: Any?): ResponseEntity<*> {
        return super.ok(`object`)
    }

    @JvmOverloads
    protected fun <T, R> page(
        `object`: Page<T?>,
        mapper: Function<T?, R?>? = null
    ): ResponseEntity<*> {
        return super.ok(pagedResources(`object`, mapper))
    }

    @JvmOverloads
    protected fun <T, R> page(
        `object`: Collection<T?>,
        mapper: Function<T?, R?>? = null
    ): ResponseEntity<*> {
        val number = if (properties.pageable.isOneIndexedParameters) 1 else 0
        val size = `object`.size
        val collect = if (mapper == null) `object` else `object`.stream().map(mapper)
            .collect(Collectors.toList())
        val pagedResources =
            pagedResources(number, size, 1, size.toLong(), collect)
        return super.ok(pagedResources)
    }

    @JvmOverloads
    protected fun <T, R> page(
        `object`: Array<T?>,
        mapper: Function<T?, R?>? = null
    ): ResponseEntity<*> {
        val number = if (properties.pageable.isOneIndexedParameters) 1 else 0
        val size = `object`.size
        val collect =
            if (mapper == null) `object`.toList() else `object`.toList().stream().map(mapper)
                .collect(Collectors.toList())
        val pagedResources =
            pagedResources(number, size, 1, size.toLong(), collect)
        return super.ok(pagedResources)
    }

    @JvmOverloads
    fun <T, R> of(`object`: Page<T?>, mapper: Function<T?, R?>? = null): RespExtra<*>? {
        return super.of(pagedResources(`object`, mapper))
    }

    @JvmOverloads
    fun <T, R> of(`object`: Collection<T?>, mapper: Function<T?, R?>? = null): RespExtra<*>? {
        val number = if (properties.pageable.isOneIndexedParameters) 1 else 0
        val size = `object`.size
        val collect = if (mapper == null) `object` else `object`.stream().map(mapper)
            .collect(Collectors.toList())
        val pagedResources =
            pagedResources(number, size, 1, size.toLong(), collect)
        return super.of(pagedResources)
    }

    @JvmOverloads
    fun <T, R> of(`object`: Array<T?>, mapper: Function<T?, R?>? = null): RespExtra<*>? {
        val number = if (properties.pageable.isOneIndexedParameters) 1 else 0
        val size = `object`.size
        val collect =
            if (mapper == null) `object`.toList() else `object`.toList().stream().map(mapper)
                .collect(Collectors.toList())
        val pagedResources =
            pagedResources(number, size, 1, size.toLong(), collect)
        return super.of(pagedResources)
    }

    @JvmOverloads
    protected fun <T, R> pagedResources(`object`: Page<T?>, mapper: Function<T?, R?>? = null): Any {
        val number =
            if (properties.pageable.isOneIndexedParameters) `object`.number + 1 else `object`.number
        val content =
            if (mapper == null) `object`.content else `object`.content.stream().map(mapper)
                .collect(Collectors.toList())
        return pagedResources(
            number,
            `object`.size,
            `object`.totalPages,
            `object`.totalElements,
            content
        )
    }

    protected open fun <T> pagedResources(
        number: Int,
        size: Int,
        totalPages: Int,
        totalElements: Long,
        content: Collection<T?>
    ): Any {
        return PagedResources(number, size, totalPages, totalElements, content)
    }
}
