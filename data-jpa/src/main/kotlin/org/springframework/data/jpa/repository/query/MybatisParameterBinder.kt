package org.springframework.data.jpa.repository.query

import org.apache.ibatis.binding.MapperMethod.ParamMap
import org.apache.ibatis.executor.ErrorContext
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.ParameterMode
import org.apache.ibatis.reflection.ParamNameResolver
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.type.TypeException
import org.apache.ibatis.type.TypeHandlerRegistry
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.query.JpaParameters.JpaParameter
import org.springframework.data.jpa.repository.query.QueryParameterSetter.BindableQuery
import org.springframework.data.jpa.repository.query.QueryParameterSetter.ErrorHandling
import top.bettercode.summer.data.jpa.query.mybatis.MybatisParam
import top.bettercode.summer.data.jpa.support.Size
import java.util.*
import javax.persistence.Query

/**
 * @author Peter Wu
 */
internal class MybatisParameterBinder(
    private val parameters: JpaParameters,
    private val paramed: Boolean,
    private val mappedStatement: MappedStatement
) : ParameterBinder(parameters, emptyList()) {
    private val typeHandlerRegistry: TypeHandlerRegistry =
        mappedStatement.configuration.typeHandlerRegistry
    private val configuration: Configuration = mappedStatement.configuration

    override fun <T : Query> bind(
        jpaQuery: T,
        metadata: QueryParameterSetter.QueryMetadata,
        accessor: JpaParametersParameterAccessor
    ): T {
        return jpaQuery
    }

    @Deprecated("")
    override fun bind(
        query: BindableQuery, accessor: JpaParametersParameterAccessor,
        errorHandling: ErrorHandling
    ) {
    }

    public override fun bindAndPrepare(
        query: Query, metadata: QueryParameterSetter.QueryMetadata,
        accessor: JpaParametersParameterAccessor
    ): Query {
        return query
    }

    fun bind(query: BindableQuery, mybatisParam: MybatisParam) {
        ErrorContext.instance().activity("setting parameters")
            .`object`(mappedStatement.parameterMap.id)
        val errorHandling = ErrorHandling.STRICT
        val boundSql = mybatisParam.boundSql
        val parameterObject = mybatisParam.parameterObject
        val parameterMappings = boundSql.parameterMappings
        if (parameterMappings != null) {
            for (i in parameterMappings.indices) {
                val parameterMapping = parameterMappings[i]
                if (parameterMapping.mode != ParameterMode.OUT) {
                    var value: Any?
                    val propertyName = parameterMapping.property
                    value = if (boundSql.hasAdditionalParameter(propertyName)) {
                        boundSql.getAdditionalParameter(propertyName)
                    } else if (parameterObject == null) {
                        null
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.javaClass)) {
                        parameterObject
                    } else {
                        val metaObject = configuration.newMetaObject(parameterObject)
                        metaObject.getValue(propertyName)
                    }
                    try {
                        val position = i + 1
                        errorHandling.execute {
                            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                            query.setParameter(position, value)
                        }
                    } catch (e: Exception) {
                        throw TypeException(
                            "Could not set parameters for mapping: $parameterMapping. Cause: $e", e
                        )
                    }
                }
            }
        }
    }

    fun bindAndPrepare(
        query: Query,
        metadata: QueryParameterSetter.QueryMetadata,
        pageable: Pageable,
        mybatisParam: MybatisParam
    ): Query {
        bind(metadata.withQuery(query), mybatisParam)
        val size = mybatisParam.size
        if (size != null && !size.isUnlimited()) {
            query.setFirstResult(0)
            query.setMaxResults(size.size)
        }
        if (!parameters.hasPageableParameter() || pageable.isUnpaged) {
            return query
        }
        query.setFirstResult(pageable.offset.toInt())
        query.setMaxResults(pageable.pageSize)
        return query
    }

    fun bindParameterObject(accessor: JpaParametersParameterAccessor): MybatisParam {
        var size: Size? = null
        val values = accessor.values
        var bindableSize = 0
        val paramMap: MutableMap<String?, Any?> = ParamMap()
        val names = this.parameters.map { p: JpaParameter -> p.name.orElse(null) }
            .filter { obj: String? -> Objects.nonNull(obj) }.toSet()
        var params: Any? = null
        for (parameter in this.parameters.bindableParameters) {
            val parameterType = parameter.type
            val parameterIndex = parameter.index
            val value = values[parameterIndex]
            if (Size::class.java.isAssignableFrom(parameterType)) {
                size = value as Size?
            } else {
                val name = parameter.name
                name.ifPresent { s: String? -> paramMap[s] = value }
                val otherName = GENERIC_NAME_PREFIX + (bindableSize + 1)
                if (!names.contains(otherName)) {
                    paramMap[otherName] = value
                }
                if (bindableSize == 0) {
                    params = ParamNameResolver.wrapToMapIfCollection(value, name.orElse(null))
                }
                bindableSize++
            }
        }
        params = if (paramed || bindableSize > 1) paramMap else params
        val parameterObject = params
        val boundSql = mappedStatement.getBoundSql(parameterObject)
        return MybatisParam(boundSql, parameterObject, size)
    }

    companion object {
        private const val GENERIC_NAME_PREFIX = "param"
    }
}
