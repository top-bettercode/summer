package top.bettercode.summer.data.jpa

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.data.domain.*
import org.springframework.data.jpa.domain.Specification
import top.bettercode.summer.data.jpa.support.UpdateSpecification
import top.bettercode.summer.web.BaseController
import java.util.*
import java.util.function.Supplier

/**
 * @author Peter Wu
 */
open class BaseService<T : Any, ID : Any, M : BaseRepository<T, ID>>(
    @JvmField
    protected val repository: M

) : IBaseService<T, ID, M> {
    @JvmField
    protected val log: Logger? = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var springDataWebProperties: SpringDataWebProperties

    override fun getRepository(): M {
        return repository
    }

    override fun <E> findAllPageByPage(totalPages: Int, query: (Pageable) -> List<E>): List<E> {
        return findAllPageByPage(springDataWebProperties.pageable.maxPageSize, totalPages, query)
    }

    override fun <E> findAllPageByPage(
        pageSize: Int,
        totalPages: Int,
        query: (Pageable) -> List<E>
    ): List<E> {
        val results = mutableListOf<E>()
        val pageable = Pageable.ofSize(pageSize)
        val result = query(pageable)
        results.addAll(result)

        runBlocking {
            val deferredResults = mutableListOf<Deferred<Pair<Int, List<E>>>>()
            val customScope = CoroutineScope(Dispatchers.Default)
            for (i in 1 until totalPages) {
                deferredResults.add(customScope.async {
                    val content = query(PageRequest.of(i, pageSize))
                    i to content
                })
            }
            deferredResults.awaitAll()
                .sortedBy { it.first }
                .forEach { results.addAll(it.second) }
        }

        return results
    }

    @JvmOverloads
    protected fun unauth(msg: String? = null): Supplier<out RuntimeException?> {
        return BaseController.unauth(msg)
    }

    @JvmOverloads
    protected fun notFound(msg: String? = null): Supplier<out RuntimeException?> {
        return BaseController.notFound(msg)
    }

    override fun <S : T> save(s: S): S {
        return repository.save(s)
    }

    override fun <S : T> dynamicSave(s: S): S {
        return repository.saveDynamic(s)
    }

    override fun <S : T> saveAll(entities: Iterable<S>): List<S> {
        return repository.saveAll(entities)
    }

    override fun update(spec: UpdateSpecification<T>): Long {
        return repository.update(spec)
    }

    override fun <S : T> update(s: S?, spec: UpdateSpecification<T>): Long {
        return repository.update(s, spec)
    }

    override fun delete(spec: Specification<T>): Long {
        return repository.delete(spec)
    }

    override fun exists(spec: Specification<T>): Boolean {
        return repository.exists(spec)
    }

    override fun findFirst(spec: Specification<T>?): Optional<T> {
        return repository.findFirst(spec)
    }

    override fun findAll(size: Int): List<T> {
        return repository.findAll(size)
    }

    override fun findAll(offset: Long, size: Int): List<T> {
        return repository.findAll(offset, size)
    }

    override fun findAll(size: Int, sort: Sort): List<T> {
        return repository.findAll(size, sort)
    }

    override fun findAll(offset: Long, size: Int, sort: Sort): List<T> {
        return repository.findAll(offset, size, sort)
    }

    override fun findAll(spec: Specification<T>?, size: Int): List<T> {
        return repository.findAll(spec, size)
    }

    override fun findAll(spec: Specification<T>?, offset: Long, size: Int): List<T> {

        return repository.findAll(spec, offset, size)
    }

    override fun findAll(spec: Specification<T>?, size: Int, sort: Sort): List<T> {
        return repository.findAll(spec, size, sort)
    }

    override fun findAll(spec: Specification<T>?, offset: Long, size: Int, sort: Sort): List<T> {
        return repository.findAll(spec, offset, size, sort)
    }

    override fun findAll(): List<T> {
        return repository.findAll()
    }

    override fun findAll(sort: Sort): List<T> {
        return repository.findAll(sort)
    }

    override fun findAllById(ids: Iterable<ID>): List<T> {
        return repository.findAllById(ids)
    }

    override fun deleteAllById(ids: Iterable<ID>) {
        repository.deleteAllById(ids)
    }

    override fun deleteAllInBatch(entities: Iterable<T>) {
        repository.deleteAllInBatch(entities)
    }

    override fun deleteAllInBatch() {
        repository.deleteAllInBatch()
    }

    override fun findAll(pageable: Pageable): Page<T> {
        return repository.findAll(pageable)
    }

    override fun findById(id: ID): Optional<T> {
        return repository.findById(id)
    }

    override fun findFirst(sort: Sort): Optional<T> {
        return repository.findFirst(sort)
    }

    override fun existsById(id: ID): Boolean {
        return repository.existsById(id)
    }

    override fun count(): Long {
        return repository.count()
    }

    override fun deleteById(id: ID) {
        repository.deleteById(id)
    }

    override fun delete(t: T) {
        repository.delete(t)
    }

    override fun deleteAll(entities: Iterable<T>) {
        repository.deleteAll(entities)
    }

    override fun deleteAll() {
        repository.deleteAll()
    }

    override fun findOne(spec: Specification<T>?): Optional<T> {
        return repository.findOne(spec)
    }

    override fun findAll(spec: Specification<T>?): List<T> {
        return repository.findAll(spec)
    }

    override fun findAll(spec: Specification<T>?, pageable: Pageable): Page<T> {
        return repository.findAll(spec, pageable)
    }

    override fun findAll(spec: Specification<T>?, sort: Sort): List<T> {
        return repository.findAll(spec, sort)
    }

    override fun count(spec: Specification<T>?): Long {
        return repository.count(spec)
    }

    protected fun <E> newPage(page: Page<E>, content: List<E>): Page<E> {
        return PageImpl(content, page.pageable, page.totalElements)
    }
}
