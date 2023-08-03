package org.springframework.data.jpa.repository.support

import org.apache.ibatis.session.Configuration
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.data.jpa.projection.CollectionAwareProjectionFactory
import org.springframework.data.jpa.provider.PersistenceProvider
import org.springframework.data.jpa.provider.QueryExtractor
import org.springframework.data.jpa.repository.query.*
import org.springframework.data.jpa.util.JpaMetamodel
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.querydsl.EntityPathResolver
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.querydsl.QuerydslUtils
import org.springframework.data.querydsl.SimpleEntityPathResolver
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.QueryCreationListener
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.core.support.SurroundingTransactionDetectorMethodInterceptor
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider
import org.springframework.lang.Nullable
import org.springframework.util.Assert
import org.springframework.util.ReflectionUtils
import top.bettercode.summer.data.jpa.config.JpaExtProperties
import top.bettercode.summer.data.jpa.querydsl.QuerydslJpaExtPredicateExecutor
import top.bettercode.summer.data.jpa.support.SimpleJpaExtRepository
import java.io.Serializable
import java.util.*
import java.util.stream.Stream
import javax.persistence.EntityManager

/**
 * implementation of a custom [JpaRepositoryFactory] to use a custom repository base class.
 *
 * @author Peter Wu
 */
class JpaExtRepositoryFactory(
        entityManager: EntityManager,
        configuration: Configuration, jpaExtProperties: JpaExtProperties
) : RepositoryFactorySupport() {
    private val jpaExtProperties: JpaExtProperties
    private val configuration: Configuration
    private val entityManager: EntityManager
    private val extractor: QueryExtractor
    private val crudMethodMetadataPostProcessor: CrudMethodMetadataPostProcessor
    private var entityPathResolver: EntityPathResolver
    private var escapeCharacter = EscapeCharacter.DEFAULT
    private var queryMethodFactory: JpaQueryMethodFactory

    init {
        Assert.notNull(entityManager, "EntityManager must not be null!")
        this.entityManager = entityManager
        extractor = PersistenceProvider.fromEntityManager(entityManager)
        crudMethodMetadataPostProcessor = CrudMethodMetadataPostProcessor()
        entityPathResolver = SimpleEntityPathResolver.INSTANCE
        queryMethodFactory = DefaultJpaQueryMethodFactory(extractor)
        addRepositoryProxyPostProcessor(crudMethodMetadataPostProcessor)
        addRepositoryProxyPostProcessor { factory: ProxyFactory, repositoryInformation: RepositoryInformation ->
            if (isTransactionNeeded(repositoryInformation.repositoryInterface)) {
                factory.addAdvice(SurroundingTransactionDetectorMethodInterceptor.INSTANCE)
            }
        }
        if (extractor == PersistenceProvider.ECLIPSELINK) {
            addQueryCreationListener(EclipseLinkProjectionQueryCreationListener(entityManager))
        }
        this.configuration = configuration
        this.jpaExtProperties = jpaExtProperties
    }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        super.setBeanClassLoader(classLoader)
        crudMethodMetadataPostProcessor.setBeanClassLoader(classLoader)
    }

    fun setEntityPathResolver(entityPathResolver: EntityPathResolver) {
        Assert.notNull(entityPathResolver, "EntityPathResolver must not be null!")
        this.entityPathResolver = entityPathResolver
    }

    fun setEscapeCharacter(escapeCharacter: EscapeCharacter) {
        this.escapeCharacter = escapeCharacter
    }

    fun setQueryMethodFactory(queryMethodFactory: JpaQueryMethodFactory) {
        Assert.notNull(queryMethodFactory, "QueryMethodFactory must not be null!")
        this.queryMethodFactory = queryMethodFactory
    }

    override fun getTargetRepository(
            information: RepositoryInformation
    ): JpaRepositoryImplementation<*, *> {
        val repository = getTargetRepository(information, entityManager)
        repository.setRepositoryMethodMetadata(crudMethodMetadataPostProcessor.crudMethodMetadata)
        repository.setEscapeCharacter(escapeCharacter)
        return repository
    }

    protected fun getTargetRepository(
            information: RepositoryInformation,
            entityManager: EntityManager?
    ): JpaRepositoryImplementation<*, *> {
        val entityInformation: JpaEntityInformation<*, Serializable> = getEntityInformation(
                information.domainType)
        val repository = getTargetRepositoryViaReflection<Any>(information, jpaExtProperties,
                entityInformation, entityManager)
        Assert.isInstanceOf(SimpleJpaExtRepository::class.java, repository)
        return repository as JpaRepositoryImplementation<*, *>
    }

    override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> {
        return SimpleJpaExtRepository::class.java
    }

    override fun getProjectionFactory(
            classLoader: ClassLoader,
            beanFactory: BeanFactory
    ): ProjectionFactory {
        val factory = CollectionAwareProjectionFactory()
        factory.setBeanClassLoader(classLoader)
        factory.setBeanFactory(beanFactory)
        return factory
    }

    override fun getQueryLookupStrategy(@Nullable key: QueryLookupStrategy.Key?, evaluationContextProvider: QueryMethodEvaluationContextProvider): Optional<QueryLookupStrategy> {
        return Optional.of(
                JpaExtQueryLookupStrategy.create(entityManager, configuration, key, extractor, evaluationContextProvider,
                        escapeCharacter,
                        jpaExtProperties))
    }

    override fun <T, ID> getEntityInformation(domainClass: Class<T>): JpaEntityInformation<T, ID> {
        @Suppress("UNCHECKED_CAST")
        return JpaEntityInformationSupport.getEntityInformation(
                domainClass, entityManager) as JpaEntityInformation<T, ID>
    }

    override fun getRepositoryFragments(metadata: RepositoryMetadata): RepositoryFragments {
        return getRepositoryFragments(metadata, entityManager, entityPathResolver,
                crudMethodMetadataPostProcessor.crudMethodMetadata)
    }

    protected fun getRepositoryFragments(
            metadata: RepositoryMetadata,
            entityManager: EntityManager,
            resolver: EntityPathResolver, crudMethodMetadata: CrudMethodMetadata
    ): RepositoryFragments {
        val isQueryDslRepository = (QuerydslUtils.QUERY_DSL_PRESENT
                && QuerydslPredicateExecutor::class.java.isAssignableFrom(metadata.repositoryInterface))
        if (isQueryDslRepository) {
            if (metadata.isReactiveRepository) {
                throw InvalidDataAccessApiUsageException(
                        "Cannot combine Querydsl and reactive repository support in a single interface")
            }
            @Suppress("UNCHECKED_CAST") val entityInformation: JpaEntityInformation<Any, Any> = getEntityInformation(metadata.domainType as Class<Any>)
            return RepositoryFragments.just(QuerydslJpaExtPredicateExecutor(jpaExtProperties,
                    entityInformation,
                    entityManager, resolver, crudMethodMetadata))
        }
        return RepositoryFragments.empty()
    }

    private class EclipseLinkProjectionQueryCreationListener(em: EntityManager) : QueryCreationListener<AbstractJpaQuery> {
        private val metamodel: JpaMetamodel
        private var warningLogged = false

        init {
            Assert.notNull(em, "EntityManager must not be null!")
            metamodel = JpaMetamodel.of(em.metamodel)
        }

        override fun onCreation(query: AbstractJpaQuery) {
            val queryMethod = query.queryMethod
            val type = queryMethod.resultProcessor.returnedType
            if (type.isProjecting && !metamodel.isJpaManaged(type.returnedType)) {
                if (!warningLogged) {
                    log.info(ECLIPSELINK_PROJECTIONS)
                    warningLogged = true
                }
                log.info(" - {}", queryMethod)
            }
        }

        companion object {
            private const val ECLIPSELINK_PROJECTIONS = "Usage of Spring Data projections detected on persistence provider EclipseLink. Make sure the following query methods declare result columns in exactly the order the accessors are declared in the projecting interface or the order of parameters for DTOs:"
            private val log = LoggerFactory
                    .getLogger(EclipseLinkProjectionQueryCreationListener::class.java)
        }
    }

    companion object {
        private fun isTransactionNeeded(repositoryClass: Class<*>): Boolean {
            val methods = ReflectionUtils.getAllDeclaredMethods(repositoryClass)
            for (method in methods) {
                if (Stream::class.java.isAssignableFrom(method.returnType) || method.isAnnotationPresent(
                                Procedure::class.java)) {
                    return true
                }
            }
            return false
        }
    }
}
