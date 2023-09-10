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
        return super.of(pagedResources(`object`)!!)
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

    protected fun ok(`object`: Any?, mapper: Function<Any?, Any?>): ResponseEntity<*> {
        return super.ok(when (`object`) {
            is Page<*> -> {
                pagedResources(`object`, mapper)
            }

            is PageableList<*> -> {
                pagedResources(`object`.toPage(), mapper)
            }

            is Collection<*> -> {
                `object`.stream().map(mapper).collect(Collectors.toList())
            }

            is Array<*> -> {
                `object`.toList().stream().map(mapper).collect(Collectors.toList())
            }

            else -> {
                `object`
            }
        })
    }

    @JvmOverloads
    protected fun page(`object`: Any?, mapper: Function<Any?, Any?>? = null): ResponseEntity<*> {
        return super.ok(pagedResources(`object`, mapper))
    }

    @JvmOverloads
    protected fun pagedResources(`object`: Any?, mapper: Function<Any?, Any?>? = null): Any? {
        return when (`object`) {
            is Page<*> -> {
                pagedResources(`object`, mapper)
            }

            is PageableList<*> -> {
                pagedResources(`object`.toPage(), mapper)
            }

            is Collection<*> -> {
                val number = if (properties!!.pageable.isOneIndexedParameters) 1 else 0
                val size = `object`.size
                val collect = if (mapper == null) `object` else `object`.stream().map(mapper).collect(Collectors.toList())
                pagedResources(number.toLong(), size.toLong(), 1, size.toLong(), collect)
            }

            is Array<*> -> {
                val number = if (properties!!.pageable.isOneIndexedParameters) 1 else 0
                val size = `object`.size
                val collect = if (mapper == null) `object`.toList() else `object`.toList().stream().map(mapper).collect(Collectors.toList())
                pagedResources(number.toLong(), size.toLong(), 1, size.toLong(), collect)
            }

            else -> {
                `object`
            }
        }
    }

    @JvmOverloads
    protected fun pagedResources(`object`: Page<*>, mapper: Function<Any?, Any?>? = null): Any {
        val number = if (properties!!.pageable.isOneIndexedParameters) `object`.number + 1 else `object`.number
        val content = if (mapper == null) `object`.content else `object`.content.stream().map(mapper).collect(Collectors.toList())
        return pagedResources(number.toLong(), `object`.size.toLong(), `object`.totalPages.toLong(), `object`.totalElements, content)
    }

    protected open fun <T> pagedResources(number: Long, size: Long, totalPages: Long, totalElements: Long, content: T?): Any {
        return PagedResources(number, size, totalPages, totalElements, content)
    }
}
