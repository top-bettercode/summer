package org.springframework.data.jpa.repository.query

import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.data.repository.query.Param
import org.springframework.util.Assert
import top.bettercode.summer.data.jpa.query.mybatis.MybatisResultSetHandler
import top.bettercode.summer.data.jpa.query.mybatis.MybatisResultTransformer
import top.bettercode.summer.data.jpa.query.mybatis.NestedResultMapType
import top.bettercode.summer.data.jpa.support.QuerySize
import java.lang.reflect.Method

/**
 *
 * @author Peter Wu
 */
class MybatisQueryMethod(
    val mappedStatement: MappedStatement,
    isPageQuery: Boolean,
    isSliceQuery: Boolean,
    isStreamQuery: Boolean,
    method: Method
) {
    private val log = LoggerFactory.getLogger(MybatisQueryMethod::class.java)

    val paramed: Boolean
    val isModifyingQuery: Boolean
    val querySize: Int?
    val countMappedStatement: MappedStatement?
    val resultTransformer: MybatisResultTransformer?
    val nestedResultMapType: NestedResultMapType?

    init {
        MybatisResultSetHandler.validateResultMaps(mappedStatement)

        if (isPageQuery) {
            val nestedResultMapId: String? =
                MybatisResultSetHandler.findNestedResultMap(mappedStatement)
            if (nestedResultMapId != null) {
                log.info(
                    "{} may return incorrect paginated data. Please check result maps definition {}.",
                    mappedStatement.id, nestedResultMapId
                )
            }
        }

        val sqlCommandType = mappedStatement.sqlCommandType
        isModifyingQuery =
            SqlCommandType.UPDATE == sqlCommandType || SqlCommandType.DELETE == sqlCommandType || SqlCommandType.INSERT == sqlCommandType

        paramed =
            method.parameterAnnotations.any { it.any { anno -> anno.annotationClass == Param::class } }

        val querySizeAnno =
            AnnotatedElementUtils.findMergedAnnotation(method, QuerySize::class.java)
        if (querySizeAnno != null) {
            val value = querySizeAnno.value
            Assert.isTrue(value > 0, "size 必须大于0")
            this.querySize = value
        } else {
            this.querySize = null
        }

        this.countMappedStatement = if (isPageQuery) try {
            mappedStatement.configuration.getMappedStatement(mappedStatement.id + "_COUNT")
        } catch (e: Exception) {
            null
        } else null

        this.resultTransformer = MybatisResultTransformer(mappedStatement)

        nestedResultMapType = if (isSliceQuery) {
            MybatisResultSetHandler.findNestedResultMapType(mappedStatement)
        } else {
            null
        }

    }
}